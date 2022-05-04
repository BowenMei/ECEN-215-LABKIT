package com.example.blue2_1.ui.ohmmeter;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.blue2_1.AndroidBluetoothService;
import com.example.blue2_1.MainActivity;
import com.example.blue2_1.R;
import com.example.blue2_1.databinding.FragmentOhmmeterBinding;

import java.util.Random;

public class OhmmeterFragment extends Fragment {
    private static final String TAG = "Ohmmeter";

    private OhmmeterViewModel ohmmeterViewModel;
    private FragmentOhmmeterBinding binding;

    private TextView text_resistanceNumber;
    private Button button_startOhmmeter;
    private Button button_resetOhmmeter;
    private Spinner spinner_unitOhmmeter;
    private int unitCase;

    boolean reset;
    String encodedVal;
    Context mContext;

    public void OnCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    private BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            encodedVal = intent.getStringExtra("Ohm-message");
            Log.d(TAG,"Received Ohm-message");
        }
    };

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mContext = this.getContext();

        ohmmeterViewModel =
                new ViewModelProvider(this).get(OhmmeterViewModel.class);

        binding = FragmentOhmmeterBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        LocalBroadcastManager.getInstance(this.getContext()).registerReceiver(messageReceiver, new IntentFilter("Ohmmeter-message"));

        text_resistanceNumber = binding.resistanceNumber;
        button_startOhmmeter = binding.StartOhmmeter;
        button_resetOhmmeter = binding.ResetOhmmeter;
        spinner_unitOhmmeter = binding.resistanceUnitsSpinner;

        ohmmeterViewModel.get_text_resistanceNumber().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                text_resistanceNumber.setText(s);
            }
        });

        button_startOhmmeter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mIntent = new Intent("receive-message");
                mIntent.putExtra("case-message", "ohmmeter");
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(mIntent);
                Log.d(TAG,"Sent case-message");
                reset = false;
                content();
            }
        });

        button_resetOhmmeter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mIntent = new Intent("receive-message");
                mIntent.putExtra("case-message", "none");
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(mIntent);
                Log.d(TAG,"Sent destroy case-message");
                reset = true;
            }
        });

        ArrayAdapter adapter = ArrayAdapter.createFromResource(this.getContext(), R.array.resistance_units, R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.modified_spinner);
        spinner_unitOhmmeter.setAdapter(adapter);

        spinner_unitOhmmeter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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
        mIntent.putExtra("case-message", "none");
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(mIntent);
        Log.d(TAG,"Sent destroy case-message");
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(messageReceiver);
        super.onDestroyView();
        binding = null;
    }

    public void content(){
        text_resistanceNumber.setText(generateData(encodedVal));

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

    public String generateData(String ohmVal){
        String newString = "";
        if(ohmVal != null) {
            try {
                int encodedInt = Integer.parseInt(ohmVal);
                double resistance = 409500 - (50*encodedInt);
                if(unitCase == 0){
                    String formatted = String.format("%.0f", resistance);
                    newString = formatted + " Ω";
                }
                if(unitCase == 1){
                    resistance = resistance/1000;
                    String formatted = String.format("%.3f", resistance);
                    newString = formatted + " kΩ";
                }
            }
            catch(NumberFormatException e){
                Log.d(TAG, "Caught number format exception");
            }
        }
        else{
            if (unitCase == 0){
                newString = "0 Ω";
            }
            if (unitCase == 1){
                newString = "0 kΩ";
            }
        }

        return newString;
        //return String.valueOf(10+10*Double.valueOf(formatted))+ " Ohm";
    }
}
