package com.example.blue2_1.ui.home;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.Button;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.blue2_1.BluetoothConnectionService;

import java.nio.charset.Charset;
import java.util.UUID;

public class HomeViewModel extends ViewModel {
    private BluetoothAdapter bluetoothAdapter;

    int x;
    private MutableLiveData<String> mText;
    private MutableLiveData<String> bluetoothInfo;

    public HomeViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue(String.valueOf(x));

        bluetoothInfo = new MutableLiveData<>();
        bluetoothInfo.setValue("");
    }

    public LiveData<String> getText() { return mText; }

    public LiveData<String> getBluetoothInfo() {return bluetoothInfo;}

    public void changeBluetoothInfo(String name, String Address) {
        bluetoothInfo.setValue(name + " " + Address);
    }
    public void changeX(){
        x++;
        mText.setValue(String.valueOf(x));
    }
}