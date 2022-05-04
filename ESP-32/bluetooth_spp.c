/*
   This example code is in the Public Domain (or CC0 licensed, at your option.)

   Unless required by applicable law or agreed to in writing, this
   software is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
   CONDITIONS OF ANY KIND, either express or implied.
*/

#include <stdint.h>
#include <string.h>
#include <stdbool.h>
#include <stdio.h>
#include "nvs.h"
#include "nvs_flash.h"
#include "freertos/FreeRTOS.h"
#include "freertos/task.h"
#include "esp_log.h"
#include "esp_bt.h"
#include "esp_bt_main.h"
#include "esp_gap_bt_api.h"
#include "esp_bt_device.h"
#include "esp_spp_api.h"
#include "driver/uart.h"
#include "driver/gpio.h"
#include "sdkconfig.h"

#include "time.h"
#include "sys/time.h"

#define SPP_TAG "SPP_Service"
#define SPP_SERVER_NAME "SPP_SERVER"
#define EXAMPLE_DEVICE_NAME "BLUE2"
#define SPP_SHOW_DATA 0
#define SPP_SHOW_SPEED 1
    #define SPP_SHOW_MODE SPP_SHOW_DATA    /*Choose show mode: show data or speed*/

#define ECHO_TEST_TXD (17)
#define ECHO_TEST_RXD (16)
#define ECHO_TEST_RTS (UART_PIN_NO_CHANGE)
#define ECHO_TEST_CTS (UART_PIN_NO_CHANGE)

#define ECHO_UART_PORT_NUM      (2)
#define ECHO_UART_BAUD_RATE     (115200)
#define ECHO_TASK_STACK_SIZE    (2048)

#define BUF_SIZE (1024)

static const esp_spp_mode_t esp_spp_mode = ESP_SPP_MODE_CB;

static struct timeval time_old;

static const esp_spp_sec_t sec_mask = ESP_SPP_SEC_AUTHENTICATE;
static const esp_spp_role_t role_master = ESP_SPP_ROLE_MASTER;

#if (SPP_SHOW_MODE == SPP_SHOW_DATA)
#define SPP_DATA_LEN 20
#else
#define SPP_DATA_LEN ESP_SPP_MAX_MTU
#endif
static uint8_t spp_data[SPP_DATA_LEN];

int a = 0;

uint8_t* write_data;
uint8_t* read_data;
int read_data_length;
bool writeSPP;
bool readSPP;

bool sentToPIC;
bool receiveFromPIC;

bool doNothing;
TaskHandle_t xHandle;


static void uart_task(void *arg)
{
    /* Configure parameters of an UART driver,
     * communication pins and install the driver */
    uart_config_t uart_config = {
        .baud_rate = ECHO_UART_BAUD_RATE,
        .data_bits = UART_DATA_8_BITS,
        .parity    = UART_PARITY_DISABLE,
        .stop_bits = UART_STOP_BITS_1,
        .flow_ctrl = UART_HW_FLOWCTRL_DISABLE,
        .source_clk = UART_SCLK_APB,
    };
    int intr_alloc_flags = 0;

#if CONFIG_UART_ISR_IN_IRAM
    intr_alloc_flags = ESP_INTR_FLAG_IRAM;
#endif

    ESP_ERROR_CHECK(uart_driver_install(ECHO_UART_PORT_NUM, BUF_SIZE * 2, 0, 0, NULL, intr_alloc_flags));
    ESP_ERROR_CHECK(uart_param_config(ECHO_UART_PORT_NUM, &uart_config));
    ESP_ERROR_CHECK(uart_set_pin(ECHO_UART_PORT_NUM, ECHO_TEST_TXD, ECHO_TEST_RXD, ECHO_TEST_RTS, ECHO_TEST_CTS));

    // Configure a temporary buffer for the incoming data

    while (1) {
        if(writeSPP){
            int len = uart_read_bytes(ECHO_UART_PORT_NUM, write_data, 1024-1, 20/portTICK_PERIOD_MS);
            sentToPIC = true;
            receiveFromPIC = false;
            writeSPP = false;
            doNothing = false;
            ESP_LOGI(SPP_TAG, "Read from UART");
        }
        
        else if(readSPP){
            uart_write_bytes(ECHO_UART_PORT_NUM, read_data, read_data_length);
            receiveFromPIC = true;
            sentToPIC = false;
            readSPP = false;
            doNothing = false;
            ESP_LOGI(SPP_TAG, "Write to UART");
        }
        else if(doNothing){
            //uart_write_bytes(ECHO_UART_PORT_NUM, read_data, read_data_length);
            //ESP_LOGI(SPP_TAG, "DO NOTHING");
            vTaskDelay(1000/portTICK_PERIOD_MS);
        }
        else{
            ESP_LOGI(SPP_TAG, "DO NOTHING");
            doNothing = true;
            vTaskDelay(1000/portTICK_PERIOD_MS);
        }
    }
}

static void esp_spp_cb(esp_spp_cb_event_t event, esp_spp_cb_param_t *param)
{
    switch (event) {
    case ESP_SPP_INIT_EVT:
        ESP_LOGI(SPP_TAG, "ESP_SPP_INIT_EVT");
        esp_spp_start_srv(sec_mask,role_master, 0, SPP_SERVER_NAME);
        break;
    case ESP_SPP_DISCOVERY_COMP_EVT:
        ESP_LOGI(SPP_TAG, "ESP_SPP_DISCOVERY_COMP_EVT");
        break;
    case ESP_SPP_OPEN_EVT:
        ESP_LOGI(SPP_TAG, "ESP_SPP_OPEN_EVT");
        esp_spp_write(param->open.handle, SPP_DATA_LEN, spp_data);
        gettimeofday(&time_old, NULL);
        break;
    case ESP_SPP_CLOSE_EVT:
        ESP_LOGI(SPP_TAG, "ESP_SPP_CLOSE_EVT");
        break;
    case ESP_SPP_START_EVT:
        ESP_LOGI(SPP_TAG, "ESP_SPP_START_EVT");
        esp_bt_dev_set_device_name(EXAMPLE_DEVICE_NAME);
        esp_bt_gap_set_scan_mode(ESP_BT_CONNECTABLE, ESP_BT_GENERAL_DISCOVERABLE);
        break;
    case ESP_SPP_CL_INIT_EVT:
        ESP_LOGI(SPP_TAG, "ESP_SPP_CL_INIT_EVT");
        break;
    case ESP_SPP_DATA_IND_EVT:
#if (SPP_SHOW_MODE == SPP_SHOW_DATA)
            ESP_LOGI(SPP_TAG, "ESP_SPP_DATA_IND_EVT len=%d handle=%d",
                 param->data_ind.len, param->data_ind.handle);

        if(((param->data_ind.len) == 1) || (param->data_ind.len) == 2){
            esp_log_buffer_char("",param->data_ind.data,param->data_ind.len);
            if(receiveFromPIC){
                readSPP = true;
                read_data = param->data_ind.data;
                read_data_length = param->data_ind.len;
            }

        }
        //esp_log_buffer_hex("",param->data_ind.data,param->data_ind.len);
        
        if((param->data_ind.len) == 3){
            read_data = param->data_ind.data;
            read_data_length = param->data_ind.len;
            if(!sentToPIC){
                xTaskAbortDelay(xHandle);
                readSPP = true;
                writeSPP = true;
                if(write_data != NULL)
                    esp_spp_write(param->write.handle, sizeof write_data, write_data);
            }
            else if(!receiveFromPIC){
                xTaskAbortDelay(xHandle);
                writeSPP = true;
                if(write_data != NULL)
                    esp_spp_write(param->write.handle, sizeof write_data, write_data);
            }
        }
            
/*
            char e[5];
            snprintf(e, sizeof e, "%d", a);
            a++;

            if((param->data_ind.data) == 100){
                char b[2] = "v";
                strcat(b, e);
                esp_spp_write(param->write.handle, sizeof b, (uint8_t*)b);
            }

            if((param->data_ind.data) == 200){
                char b[2] = "c";
                strcat(b, e);
                esp_spp_write(param->write.handle, sizeof b, (uint8_t*)b);
            }

            if((param->data_ind.data) == 300){
                char b[2] = "r";
                strcat(b, e);
                esp_spp_write(param->write.handle, sizeof b, (uint8_t*)b);
            }

            if((param->data_ind.data) == 400){
                char b[2] = "w";
                strcat(b, e);
                esp_spp_write(param->write.handle, sizeof b, (uint8_t*)b);
            }
            
            if((param->data_ind.data) == 500){
                char b[2] = "o";
                strcat(b, e);
                esp_spp_write(param->write.handle, sizeof b, (uint8_t*)b);  
            }

            if((param->data_ind.data) == 600){
                char b[2] = "O";
                strcat(b, e);
                esp_spp_write(param->write.handle, sizeof b, (uint8_t*)b);
            }
*/           
/*
            if((a >= 0.0) && (a < 10.0)){
                char e[6];
                snprintf(e, sizeof e, "%1.3f", a);
                esp_spp_write(param->write.handle, sizeof e, (uint8_t*)e);
                a = a + 0.001;
            }
            if((a >= 10.0) && (a < 100.0)){
                char e[7];
                snprintf(e, sizeof e, "%1.3f", a);
                esp_spp_write(param->write.handle, sizeof e, (uint8_t*)e);
                a = a + 0.001;
            }
            if((a > -10.0) && (a <= 0.0)){
                char e[7];
                snprintf(e, sizeof e, "%1.3f", a);
                esp_spp_write(param->write.handle, sizeof e, (uint8_t*)e);
                a = a + 0.001;
            }
            if((a > -100.0) && (a <= -10.0)){
                char e[8];
                snprintf(e, sizeof e, "%1.3f", a);
                esp_spp_write(param->write.handle, sizeof e, (uint8_t*)e);
                a = a + 0.001;
            }
*/
            

#endif
        break;
    case ESP_SPP_CONG_EVT:
        ESP_LOGI(SPP_TAG, "ESP_SPP_CONG_EVT");
        break;
    case ESP_SPP_WRITE_EVT:
        ESP_LOGI(SPP_TAG, "ESP_SPP_WRITE_EVT");
        break;
    case ESP_SPP_SRV_OPEN_EVT:
        ESP_LOGI(SPP_TAG, "ESP_SPP_SRV_OPEN_EVT");
        gettimeofday(&time_old, NULL);
        break;
    case ESP_SPP_SRV_STOP_EVT:
        ESP_LOGI(SPP_TAG, "ESP_SPP_SRV_STOP_EVT");
        break;
    case ESP_SPP_UNINIT_EVT:
        ESP_LOGI(SPP_TAG, "ESP_SPP_UNINIT_EVT");
        break;
    default:
        break;
    }
}

void esp_bt_gap_cb(esp_bt_gap_cb_event_t event, esp_bt_gap_cb_param_t *param)
{
    switch (event) {
    case ESP_BT_GAP_AUTH_CMPL_EVT:{
        if (param->auth_cmpl.stat == ESP_BT_STATUS_SUCCESS) {
            ESP_LOGI(SPP_TAG, "authentication success: %s", param->auth_cmpl.device_name);
            esp_log_buffer_hex(SPP_TAG, param->auth_cmpl.bda, ESP_BD_ADDR_LEN);
        } else {
            ESP_LOGE(SPP_TAG, "authentication failed, status:%d", param->auth_cmpl.stat);
        }
        break;
    }
    case ESP_BT_GAP_PIN_REQ_EVT:{
        ESP_LOGI(SPP_TAG, "ESP_BT_GAP_PIN_REQ_EVT min_16_digit:%d", param->pin_req.min_16_digit);
        if (param->pin_req.min_16_digit) {
            ESP_LOGI(SPP_TAG, "Input pin code: 0000 0000 0000 0000");
            esp_bt_pin_code_t pin_code = {0};
            esp_bt_gap_pin_reply(param->pin_req.bda, true, 16, pin_code);
        } else {
            ESP_LOGI(SPP_TAG, "Input pin code: 1234");
            esp_bt_pin_code_t pin_code;
            pin_code[0] = '1';
            pin_code[1] = '2';
            pin_code[2] = '3';
            pin_code[3] = '4';
            esp_bt_gap_pin_reply(param->pin_req.bda, true, 4, pin_code);
        }
        break;
    }

#if (CONFIG_BT_SSP_ENABLED == true)
    case ESP_BT_GAP_CFM_REQ_EVT:
        ESP_LOGI(SPP_TAG, "ESP_BT_GAP_CFM_REQ_EVT Please compare the numeric value: %d", param->cfm_req.num_val);
        esp_bt_gap_ssp_confirm_reply(param->cfm_req.bda, true);
        break;
    case ESP_BT_GAP_KEY_NOTIF_EVT:
        ESP_LOGI(SPP_TAG, "ESP_BT_GAP_KEY_NOTIF_EVT passkey:%d", param->key_notif.passkey);
        break;
    case ESP_BT_GAP_KEY_REQ_EVT:
        ESP_LOGI(SPP_TAG, "ESP_BT_GAP_KEY_REQ_EVT Please enter passkey!");
        break;
#endif

    case ESP_BT_GAP_MODE_CHG_EVT:
        ESP_LOGI(SPP_TAG, "ESP_BT_GAP_MODE_CHG_EVT mode:%d", param->mode_chg.mode);
        break;

    default: {
        ESP_LOGI(SPP_TAG, "event: %d", event);
        break;
    }
    return;
    }
}

void app_main(void)
{   
    sentToPIC = false;
    receiveFromPIC = false;
    xHandle = NULL;
    xTaskCreate(uart_task, "uart_task", ECHO_TASK_STACK_SIZE, NULL, 10, &xHandle);
    configASSERT(xHandle);

    esp_err_t ret = nvs_flash_init();
    
    if (ret == ESP_ERR_NVS_NO_FREE_PAGES || ret == ESP_ERR_NVS_NEW_VERSION_FOUND) {
        ESP_ERROR_CHECK(nvs_flash_erase());
        ret = nvs_flash_init();
    }
    ESP_ERROR_CHECK( ret );

    ESP_ERROR_CHECK(esp_bt_controller_mem_release(ESP_BT_MODE_BLE));

    esp_bt_controller_config_t bt_cfg = BT_CONTROLLER_INIT_CONFIG_DEFAULT();
    if ((ret = esp_bt_controller_init(&bt_cfg)) != ESP_OK) {
        ESP_LOGE(SPP_TAG, "%s initialize controller failed: %s\n", __func__, esp_err_to_name(ret));
        return;
    }

    if ((ret = esp_bt_controller_enable(ESP_BT_MODE_CLASSIC_BT)) != ESP_OK) {
        ESP_LOGE(SPP_TAG, "%s enable controller failed: %s\n", __func__, esp_err_to_name(ret));
        return;
    }

    if ((ret = esp_bluedroid_init()) != ESP_OK) {
        ESP_LOGE(SPP_TAG, "%s initialize bluedroid failed: %s\n", __func__, esp_err_to_name(ret));
        return;
    }

    if ((ret = esp_bluedroid_enable()) != ESP_OK) {
        ESP_LOGE(SPP_TAG, "%s enable bluedroid failed: %s\n", __func__, esp_err_to_name(ret));
        return;
    }

    if ((ret = esp_bt_gap_register_callback(esp_bt_gap_cb)) != ESP_OK) {
        ESP_LOGE(SPP_TAG, "%s gap register failed: %s\n", __func__, esp_err_to_name(ret));
        return;
    }

    if ((ret = esp_spp_register_callback(esp_spp_cb)) != ESP_OK) {
        ESP_LOGE(SPP_TAG, "%s spp register failed: %s\n", __func__, esp_err_to_name(ret));
        return;
    }

    if ((ret = esp_spp_init(esp_spp_mode)) != ESP_OK) {
        ESP_LOGE(SPP_TAG, "%s spp init failed: %s\n", __func__, esp_err_to_name(ret));
        return;
    }

#if (CONFIG_BT_SSP_ENABLED == true)
    /* Set default parameters for Secure Simple Pairing */
    esp_bt_sp_param_t param_type = ESP_BT_SP_IOCAP_MODE;
    esp_bt_io_cap_t iocap = ESP_BT_IO_CAP_IO;
    esp_bt_gap_set_security_param(param_type, &iocap, sizeof(uint8_t));
#endif

    /*
     * Set default parameters for Legacy Pairing
     * Use variable pin, input pin code when pairing
     */
    esp_bt_pin_type_t pin_type = ESP_BT_PIN_TYPE_VARIABLE;
    esp_bt_pin_code_t pin_code;
    esp_bt_gap_set_pin(pin_type, 0, pin_code);
}
