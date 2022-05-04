package com.example.blue2_1.ui.waveformGenerator;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.blue2_1.R;
import com.example.blue2_1.databinding.FragmentWaveformgeneratorBinding;

import java.lang.reflect.Array;

public class WaveformGeneratorFragment extends Fragment {
    private static final String TAG = "Waveform Generator";

    private WaveformGeneratorViewModel waveformGeneratorViewModel;
    private FragmentWaveformgeneratorBinding binding;

    private Button button_startWaveform;
    private Button button_stopWaveform;
    private Spinner spinner_waveform;
    private String unitCase;

    Context mContext;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mContext = this.getContext();
        waveformGeneratorViewModel =
                new ViewModelProvider(this).get(WaveformGeneratorViewModel.class);

        binding = FragmentWaveformgeneratorBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        spinner_waveform = binding.waveformSpinner;
        button_startWaveform  = binding.GenerateWaveform;
        button_stopWaveform = binding.StopWaveform;

        ArrayAdapter adapter = ArrayAdapter.createFromResource(this.getContext(), R.array.waveforms, R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.modified_spinner);
        spinner_waveform.setAdapter(adapter);

        spinner_waveform.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case(0):
                        unitCase = "1";
                        break;
                    case(1):
                        unitCase = "2";
                        break;
                    case(2):
                        unitCase = "3";
                        break;
                    case(3):
                        unitCase = "4";
                        break;
                    case(4):
                        unitCase = "5";
                        break;
                    case(5):
                        unitCase = "6";
                        break;
                    case(6):
                        unitCase = "7";
                        break;
                    case(7):
                        unitCase = "8";
                        break;
                    case(8):
                        unitCase = "9";
                        break;
                    case(9):
                        unitCase = "A";
                        break;
                    case(10):
                        unitCase = "B";
                        break;
                    case(11):
                        unitCase = "C";
                        break;
                    case(12):
                        unitCase = "D";
                        break;
                    case(13):
                        unitCase = "E";
                        break;
                    case(14):
                        unitCase = "F";
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }

        });

        button_startWaveform.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mIntent = new Intent("receive-message");
                mIntent.putExtra("case-message", "waveformGenerator");
                mIntent.putExtra("waveform-case", unitCase);
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(mIntent);
                Log.d(TAG, "Sent case-message");
            }
        });

        button_stopWaveform.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mIntent = new Intent("receive-message");
                mIntent.putExtra("case-message", "end-waveformGenerator");
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(mIntent);
                Log.d(TAG, "Sent destroy case-message");
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
        super.onDestroyView();
        binding = null;
    }
}