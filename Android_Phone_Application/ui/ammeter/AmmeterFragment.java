package com.example.blue2_1.ui.ammeter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.blue2_1.R;
import com.example.blue2_1.databinding.FragmentAmmeterBinding;

import java.util.Random;

public class AmmeterFragment extends Fragment {
    private static final String TAG = "Ammeter";

    private AmmeterViewModel ammeterViewModel;
    private FragmentAmmeterBinding binding;

    private TextView text_currentNumber;
    private Button button_startAmmeter;
    private Button button_resetAmmeter;
    private Spinner spinner_unitAmmeter;
    private int unitCase;

    boolean reset;
    String encodedVal;
    Context mContext;

    private BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            encodedVal = intent.getStringExtra("Current-message");
            Log.d(TAG, "Received Current-message");
        }
    };

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mContext = this.getContext();

        ammeterViewModel =
                new ViewModelProvider(this).get(AmmeterViewModel.class);

        binding = FragmentAmmeterBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        LocalBroadcastManager.getInstance(this.getContext()).registerReceiver(messageReceiver,new IntentFilter("Ammeter-message"));

        text_currentNumber = binding.currentNumber;
        button_startAmmeter = binding.StartAmmeter;
        button_resetAmmeter = binding.ResetAmmeter;
        spinner_unitAmmeter = binding.currentUnitsSpinner;

        ammeterViewModel.get_text_currentNumber().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                text_currentNumber.setText(s);
            }
        });

        button_resetAmmeter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mIntent = new Intent("receive-message");
                mIntent.putExtra("case-message", "end-ammeter");
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(mIntent);
                Log.d(TAG,"Sent destroy case-message");
                reset = true;
            }
        });

        button_startAmmeter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mIntent = new Intent("receive-message");
                mIntent.putExtra("case-message", "ammeter");
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(mIntent);
                Log.d(TAG, "Sent case-message");
                reset = false;
                content();
            }
        });

        ArrayAdapter adapter = ArrayAdapter.createFromResource(this.getContext(), R.array.current_units, R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.modified_spinner);
        spinner_unitAmmeter.setAdapter(adapter);

        spinner_unitAmmeter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch(position){
                    case(0):
                        unitCase = 0;
                        break;
                    case(1):
                        unitCase = 1;
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        reset = true;
        Intent mIntent = new Intent("receive-message");
        mIntent.putExtra("case-message", "end-ammeter");
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(mIntent);
        Log.d(TAG,"Sent destroy case-message");
        LocalBroadcastManager.getInstance(this.getContext()).unregisterReceiver(messageReceiver);
        super.onDestroyView();
        binding = null;
    }

    public void content(){
        text_currentNumber.setText(generateData(encodedVal));
        Log.d(TAG, "Generating Data " + encodedVal);
        refresh(1000);
    }

    private void refresh(int ms){
        final Handler mHandler = new Handler();
        final Runnable runnable = new Runnable(){
            @Override
            public void run() {
                if(!reset)
                content();
            }
        };
        if(!reset)
            mHandler.postDelayed(runnable,ms);
    }

    public String generateData(String currentVal){

        String newString = "";
        if(currentVal != null){
            try{
                int encodedInt = Integer.parseInt(currentVal);
                double current = (0.8192/8192.0) * encodedInt;
                if(unitCase == 0){
                    String formatted = String.format("%.3f", current);
                    newString = formatted + " mA";
                }
                if(unitCase == 1){
                    current = current*1000;
                    String formatted = String.format("%.0f", current);
                    newString = formatted + " uA";
                }
            }
            catch (NumberFormatException e){
                Log.d(TAG, "Caught number format exception");
            }
        }
        else{
            if (unitCase == 0){
                newString = "0.000 mA";
            }
            if (unitCase == 1){
                newString = "0000 uA";
            }
        }


        return newString;
    }
}