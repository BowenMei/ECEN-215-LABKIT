package com.example.blue2_1;

import android.app.Service;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import androidx.core.content.res.TypedArrayUtils;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Array;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

public class AndroidBluetoothService extends Service {
    IBinder mBinder = new LocalBinder();
    Context mContext;
    private static final String TAG = "Bluetooth Service";

    private BluetoothDevice bluetoothDevice;
    private BluetoothAdapter bluetoothAdapter;

    private Handler mHandler = new Handler();

    private String NAME = "BluetoothService";
    private static final UUID MY_UUID =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    //UUID.fromString("af720eb4-0c40-0d01-63a3-6d534bc20032");

    private interface MessageConstant{
        public static final int MESSAGE_READ = 0;
        public static final int MESSAGE_WRITE = 1;
        public static final int MESSAGE_TOAST = 2;

        //add stuff if needed
    }

    public AndroidBluetoothService() {
        turnOnBluetooth();
        connectDevices();
        int operation = isServerOrClient();
        if(operation == 0){
            Log.d(TAG, "AcceptThread, Entering");
            AcceptThread newAcceptThread = new AcceptThread();
            newAcceptThread.start();
        }
        else{
            Log.d(TAG, "ConnectThread, Entering");
            ConnectThread newConnectThread = new ConnectThread(bluetoothDevice);
            newConnectThread.start();
        }
        mContext = this;
    }

    private int isServerOrClient() {
        int returnVal = 1;
        if(returnVal == 0){
            Log.d(TAG, "Bluetooth Service is Server");
        }
        else{
            Log.d(TAG, "Bluetooth service is Client");
        }
        return 1;
    }

    private void turnOnBluetooth() {
        Log.d(TAG, "Entering turnOnBluetooth");
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(!bluetoothAdapter.isEnabled()){
            bluetoothAdapter.enable();
        }
        Log.d(TAG, "Exiting turnOnBluetooth");
    }

    private void connectDevices() {
        Log.d(TAG, "Entering connectingDevices");
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if(pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress();
                if (deviceName.equals("BLUE2"))
                    bluetoothDevice = device;
            }
        }
        Log.d(TAG, "Exiting connectingDevices");
        //scan for new devices and add that to the pairedDevices
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class LocalBinder extends Binder {
        public AndroidBluetoothService getAndroidInstance(){
            return AndroidBluetoothService.this;
        }
    }

    public class AcceptThread extends Thread{
        private static final String TAG = "AcceptThread";
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread(){
            BluetoothServerSocket tmp = null;
            try{
                tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
            }
            catch(IOException e){}
            mmServerSocket = tmp;
        }

        public void run(){
            BluetoothSocket socket = null;
            while(true){
                try{
                    socket = mmServerSocket.accept();
                }
                catch(IOException e){
                    break;
                }
                if(socket != null){
                    manageSocket newServerSocketManager = new manageSocket(socket);
                    try {
                        mmServerSocket.close();
                    } catch (IOException e) {}
                    break;
                }
            }
        }

        public void cancel() {
            try{
                mmServerSocket.close();
            }catch(IOException e){}
        }
    }

    public class ConnectThread extends Thread{
        private static final String TAG = "ConnectThread";

        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device){
            Log.d(TAG, "ConnectThread, Entering");
            BluetoothSocket tmp = null;
            mmDevice = device;

            try{
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
                Log.d(TAG, "ConnectThread, Created the tmp Socket");
            } catch (IOException e){
                Log.d(TAG, "ConnectThread, Failed to Create the tmp Socket");
            }
            mmSocket = tmp;

            Log.d(TAG, "ConnectThread, Exiting");
        }

        public void run(){
            bluetoothAdapter.cancelDiscovery();
            Log.d(TAG, "ConnectThread, Running");
            try{
                Log.d(TAG, "ConnectedThread, Trying Connection");
                mmSocket.connect();
            } catch (IOException connectionException){
                try{
                    mmSocket.close();
                    Log.d(TAG, "Closed in ConnectedThread, Failed Connection");
                } catch(IOException closeException){
                    Log.d(TAG, "Unable to close connection");
                }
                return;
            }
            Log.d(TAG, "Opening Manage Socket");
            manageSocket newClientSocketManager = new manageSocket(mmSocket);
            newClientSocketManager.start();
        }

        public void cancel(){
            try{
                mmSocket.close();
                Log.d(TAG, "Closed in ConnectedThread, Cancel Method");
            }catch (IOException e){}
        }
    }

    public class manageSocket extends Thread{
        private static final String TAG = "ManageThread";

        private final BluetoothSocket mmSocket;
        private final InputStream mmInputStream;
        private final OutputStream mmOutputStream;
        private byte[] mmBuffer;
        private byte[] oldBuffer;

        String caseVal="";
        String waveformVal = "0";
        String previousVal = "0";
        int startReceiver = 0;

        public manageSocket(BluetoothSocket socket){
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try{
                tmpIn = socket.getInputStream();
            }catch(IOException e){}
            try{
                tmpOut = socket.getOutputStream();
            }catch(IOException e){}

            mmInputStream = tmpIn;
            mmOutputStream = tmpOut;
        }

        public BroadcastReceiver messageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                caseVal = intent.getStringExtra("case-message");
                if(intent.hasExtra("waveform-case")){
                    waveformVal = intent.getStringExtra("waveform-case");
                    Log.d(TAG, "waveform value: "+ waveformVal);
                }
                Log.d(TAG,"case value: "+caseVal);
            }
        };

        public void run(){
            mmBuffer = new byte[1024];
            oldBuffer = new byte[1024];
            boolean startStream = true;
            int numBytes = 0;
            if(startReceiver == 0){
                LocalBroadcastManager.getInstance(mContext).registerReceiver(messageReceiver, new IntentFilter("receive-message"));
                startReceiver = 1;
            }

            while(true){
                try{
                    BluetoothSocket tmpsocket = mmSocket;
                    OutputStream tmpOutStream = tmpsocket.getOutputStream();
                    InputStream tmpInStream = tmpsocket.getInputStream();
                    if(caseVal.equals("ohmmeter")) {
                        if(startStream) {
                            write("5-1", tmpOutStream);
                            startStream = false;
                        }
                        if (tmpInStream.available()!=0) {
                            numBytes = tmpInStream.read(mmBuffer);
                            String stringStream = new String(mmBuffer);
                            String[] typeAndValue = stringStream.split("-");
                            if(typeAndValue[0].equals("5")){
                                String stringVal = typeAndValue[1].replaceAll("[^0-9]","");
                                //mmInputStream.skip(numBytes);
                                try {
                                    int e = Integer.parseInt(stringVal);
                                    Intent mIntent = new Intent("Ohmmeter-message");
                                    mIntent.putExtra("Ohm-message", stringVal);
                                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(mIntent);
                                    write("5-1", tmpOutStream);
                                }
                                catch (NumberFormatException e){
                                    if(tmpInStream.available() != 0) {
                                        int trash = tmpInStream.read(oldBuffer);
                                    }
                                    Log.d(TAG, "Cleared buffer");
                                    write("5-1", tmpOutStream);
                                }
                            }

                        }
                    }
                    if(caseVal.equals("ammeter")) {
                        if(startStream){
                            write("4-1",tmpOutStream);
                            startStream = false;
                        }
                        if (tmpInStream.available()!=0) {
                            numBytes = tmpInStream.read(mmBuffer);
                            String stringStream = new String(mmBuffer);
                            String[] typeAndValue = stringStream.split("-");
                            if(typeAndValue[0].equals("4")) {
                                String stringVal = typeAndValue[1].replaceAll("[^0-9]", "");
                                //mmInputStream.skip(numBytes);
                                try {
                                    int e = Integer.parseInt(stringVal);
                                    Intent mIntent = new Intent("Ammeter-message");
                                    mIntent.putExtra("Current-message", stringVal);
                                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(mIntent);
                                    write("4-1", tmpOutStream);
                                } catch (NumberFormatException e) {
                                    if (tmpInStream.available() != 0) {
                                        int trash = tmpInStream.read(oldBuffer);
                                    }
                                    Log.d(TAG, "Cleared buffer");
                                    write("4-1", tmpOutStream);
                                }
                            }
                        }
                    }
                    if(caseVal.equals("voltmeter")) {
                        if(startStream) {
                            write("2-1", tmpOutStream);
                            startStream = false;
                        }
                        if (tmpInStream.available()!=0) {
                            numBytes = tmpInStream.read(mmBuffer);
                            String stringStream = new String(mmBuffer);
                            String[] typeAndValue = stringStream.split("-");
                            if(typeAndValue[0].equals("2")) {
                                String stringVal = typeAndValue[1].replaceAll("[^0-9]", "");
                                //mmInputStream.skip(numBytes);
                                try {
                                    int e = Integer.parseInt(stringVal);
                                    Intent mIntent = new Intent("Voltmeter-message");
                                    mIntent.putExtra("Voltage-message", stringVal);
                                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(mIntent);
                                    write("2-1", tmpOutStream);
                                } catch (NumberFormatException e) {
                                    if (tmpInStream.available() != 0) {
                                        int trash = tmpInStream.read(oldBuffer);
                                    }
                                    Log.d(TAG, "Cleared buffer");
                                    write("2-1", tmpOutStream);
                                }
                            }
                        }
                    }
                    if(caseVal.equals("oscilloscope")){
                        if(startStream) {
                            write("3-1", tmpOutStream);
                            startStream = false;
                        }
                        if (tmpInStream.available()!=0) {
                            numBytes = tmpInStream.read(mmBuffer);
                            String stringStream = new String(mmBuffer);
                            String[] typeAndValue = stringStream.split("-");
                            if(typeAndValue[0].equals("3")){
                                String stringVal = stringStream.replaceAll("[^0-9]","");
                                //mmInputStream.skip(numBytes);
                                Intent mIntent = new Intent("Oscilloscope-message");
                                mIntent.putExtra("Voltage-message", stringVal);
                                LocalBroadcastManager.getInstance(mContext).sendBroadcast(mIntent);
                                write("3-1", tmpOutStream);
                            }

                        }
                    }
                    if(caseVal.equals("waveformGenerator")){
                        if(waveformVal.equals(previousVal)){
                            Log.d(TAG,"Values: "+waveformVal + " " +previousVal);
                            write(waveformVal,tmpOutStream);
                            previousVal = waveformVal;
                        }

                    }

                    if(caseVal.equals("end-waveformGenerator")){
                        startStream = true;
                        write("1-0", tmpOutStream);
                    }
                    if(caseVal.equals("end-voltmeter")){
                        startStream = true;
                        write("2-0", tmpOutStream);
                    }
                    if(caseVal.equals("end-oscilloscope")){
                        startStream = true;
                        write("3-0", tmpOutStream);
                    }
                    if(caseVal.equals("end-ammeter")){
                        startStream = true;
                        write("4-0", tmpOutStream);
                    }
                    if(caseVal.equals("end-ohmmeter")){
                        startStream = true;
                        write("5-0", tmpOutStream);
                    }
                    if(caseVal.equals("none")){
                        startStream = true;
                    }
                }catch (IOException e){
                    Log.d(TAG, "FUCK");
                    break;
                }
            }
        }

       /* public void flush()throws IOException{
            while(mmInputStream.available() > 0){
                mmInputStream.read();
                String avalVal = Integer.toString(mmInputStream.available());
                Log.d(TAG, "Flushing: "+avalVal);
            }
        }*/

        public void write(String writeVal, OutputStream os){
            try {
                Log.d(TAG, "Writing: "+ writeVal);
                byte[] bytes = writeVal.getBytes();
                os.write(bytes);
            }catch(IOException e){
                Log.d(TAG, "Unable to write.");
            }
        }

        public void cancel() {
            try{
                mmSocket.close();
            }catch(IOException e){}
        }
    }
}