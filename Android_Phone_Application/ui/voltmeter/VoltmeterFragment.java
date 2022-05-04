package com.example.blue2_1.ui.voltmeter;

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
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.blue2_1.MainActivity;
import com.example.blue2_1.R;
import com.example.blue2_1.databinding.FragmentVoltmeterBinding;
import com.jjoe64.graphview.series.DataPoint;

import org.w3c.dom.Text;

import java.util.Random;

public class VoltmeterFragment extends Fragment {
    private static final String TAG = "Voltmeter";

    private VoltmeterViewModel voltmeterViewModel;
    private FragmentVoltmeterBinding binding;

    private TextView text_voltageNumber;
    private Button button_startVoltmeter;
    private Button button_resetVoltmeter;
    private Spinner spinner_unitVoltmeter;
    private int unitCase;

    boolean reset;
    String encodedVal;
    Context mContext;

    private BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            encodedVal = intent.getStringExtra("Voltage-message");
            Log.d(TAG, "Received Voltage-message");
        }
    };

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mContext = this.getContext();

        voltmeterViewModel =
                new ViewModelProvider(this).get(VoltmeterViewModel.class);

        binding = FragmentVoltmeterBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        LocalBroadcastManager.getInstance(this.getContext()).registerReceiver(messageReceiver, new IntentFilter("Voltmeter-message"));

        text_voltageNumber = binding.voltageNumber;
        button_startVoltmeter = binding.StartVoltmeter;
        button_resetVoltmeter = binding.ResetVoltmeter;
        spinner_unitVoltmeter = binding.voltageUnitsSpinner;

        voltmeterViewModel.get_text_voltageNumber().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                text_voltageNumber.setText(s);
            }

        });

        button_resetVoltmeter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mIntent = new Intent("receive-message");
                mIntent.putExtra("case-message", "none");
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(mIntent);
                Log.d(TAG,"Sent destroy case-message");
                reset = true;
            }
        });

        button_startVoltmeter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mIntent = new Intent("receive-message");
                mIntent.putExtra("case-message", "voltmeter");
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(mIntent);
                Log.d(TAG, "Sent case-message");
                reset = false;
                content();
            }
        });

        ArrayAdapter adapter = ArrayAdapter.createFromResource(this.getContext(),R.array.voltage_units, R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.modified_spinner);
        spinner_unitVoltmeter.setAdapter(adapter);

        spinner_unitVoltmeter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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
        Intent mIntent = new Intent("receive-message");
        mIntent.putExtra("case-message", "none");
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(mIntent);
        Log.d(TAG,"Sent destroy case-message");
        LocalBroadcastManager.getInstance(this.getContext()).unregisterReceiver(messageReceiver);
        super.onDestroyView();
        binding = null;
    }

    public void content(){
        text_voltageNumber.setText(generateData(encodedVal));

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

    public String generateData(String voltVal){
        String newString = "";
        if(voltVal != null){
            try {
                int encodedInt = Integer.parseInt(voltVal);
                double voltage = -5.0 + (10.0/8192.0) * encodedInt;
                if(unitCase == 0){
                    String formatted = String.format("%.3f", voltage);
                    newString = formatted + " V";
                }
                if(unitCase == 1){
                    voltage = voltage * 1000;
                    String formatted = String.format("%.0f", voltage);
                    newString = formatted + " mV";
                }
            }
            catch(NumberFormatException e){
                    Log.d(TAG, "Caught number format exception");
            }
        }
        else{
            if (unitCase == 0){
                newString = "0.000 V";
            }
            if (unitCase == 1){
                newString = "0000 mV";
            }
        }

        return newString;
    }
}