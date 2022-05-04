package com.example.blue2_1.ui.voltmeter;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.Random;

public class VoltmeterViewModel extends ViewModel {

    private MutableLiveData<String> titleText;
    private MutableLiveData<String> voltageNumber;

    public VoltmeterViewModel() throws InterruptedException {
        titleText = new MutableLiveData<>();
        titleText.setValue("");

        voltageNumber = new MutableLiveData<>();
        voltageNumber.setValue("0.0 V");

    }

    public void setVoltage(double value){
        voltageNumber.setValue(String.valueOf(value));
    }

    public LiveData<String> get_textVoltmeter() {
        return titleText;
    }
    public LiveData<String> get_text_voltageNumber() { return voltageNumber; }

}