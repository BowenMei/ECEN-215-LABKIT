package com.example.blue2_1.ui.home;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.fragment.app.*;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.blue2_1.BluetoothConnectionService;
import com.example.blue2_1.Constants;
import com.example.blue2_1.R;
import com.example.blue2_1.databinding.FragmentHomeBinding;
import com.example.blue2_1.databinding.FragmentOhmmeterBinding;
import com.example.blue2_1.ui.ohmmeter.OhmmeterViewModel;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class HomeFragment extends Fragment {
    private static final String TAG = "Home Fragment";

    private HomeViewModel homeViewModel;
    private FragmentHomeBinding binding;

    private Button openBT_button;
    private Button closeBT_button;
    private Button changeValue_button;
    private TextView value_text;
    private TextView bluetooth_text;

    String printVal;

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    private BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            printVal = intent.getStringExtra("string-message");
        }
    };

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        LocalBroadcastManager.getInstance(this.getContext()).registerReceiver(messageReceiver, new IntentFilter("my-message"));

        return root;
    }

    public void content(){
        bluetooth_text.setText(printVal);

        refresh(1000);
    }

    private void refresh(int ms){
        final Handler mHandler = new Handler();
        final Runnable runnable = new Runnable(){
            @Override
            public void run() {
                content();
            }
        };

        mHandler.postDelayed(runnable,ms);
    }

    @Override
    public void onDestroyView() {
        LocalBroadcastManager.getInstance(this.getContext()).unregisterReceiver(messageReceiver);
        super.onDestroyView();
        binding = null;
    }
}