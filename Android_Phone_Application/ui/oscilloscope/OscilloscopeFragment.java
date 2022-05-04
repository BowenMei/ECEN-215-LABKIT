package com.example.blue2_1.ui.oscilloscope;

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
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.blue2_1.databinding.FragmentOscilloscopeBinding;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.Random;

public class OscilloscopeFragment extends Fragment {
    private static final String TAG = "Oscilloscope";

    private OscilloscopeViewModel OscilloscopeViewModel;
    private FragmentOscilloscopeBinding binding;

    private final Handler mHandler = new Handler();
    private Runnable mTimer1;
    private LineGraphSeries<DataPoint> mSeries;
    private double time = 0.0;

    private GraphView OscilloscopeGraph;
    private Button button_startOscilloscope;
    private Button button_resetOscilloscope;

    String encodedVal;
    Context mContext;
    boolean newPrintVal;
    boolean destroyed = false;

    public BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            encodedVal = intent.getStringExtra("Voltage-message");
            Log.d(TAG, "Received Voltage-message");
            newPrintVal = true;
        }
    };

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mContext = this.getContext();

        OscilloscopeViewModel =
                new ViewModelProvider(this).get(OscilloscopeViewModel.class);

        binding = FragmentOscilloscopeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        LocalBroadcastManager.getInstance(this.getContext()).registerReceiver(messageReceiver, new IntentFilter("Oscilloscope-message"));

        OscilloscopeGraph = binding.oscilloscopeGraph;
        button_startOscilloscope = binding.StartOscilloscope;
        button_resetOscilloscope = binding.ResetOscilloscope;

        button_resetOscilloscope.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mIntent = new Intent("receive-message");
                mIntent.putExtra("case-message", "none");
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(mIntent);
                Log.d(TAG,"Sent destroy case-message");
            }
        });

        button_startOscilloscope.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mIntent = new Intent("receive-message");
                mIntent.putExtra("case-message", "oscilloscope");
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(mIntent);
                Log.d(TAG, "Sent case-message");
                if(mSeries == null){
                    mSeries = new LineGraphSeries<>();
                    OscilloscopeGraph.addSeries(mSeries);

                    Viewport viewport =  OscilloscopeGraph.getViewport();
                    viewport.setYAxisBoundsManual(true);
                    viewport.setMinY(-5);
                    viewport.setMaxY(5);
                    viewport.setMaxXAxisSize(1.0);
                    viewport.setScrollable(true);

                    Log.d(TAG, "Created new Graph");

                    Thread getData = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while(true){
                                addData();
                                OscilloscopeGraph.onDataChanged(true, true);
                                Log.d(TAG, "Trying to add data");
                                try{
                                    Thread.sleep(100);
                                }catch(InterruptedException e){
                                    Log.d(TAG, "FUCK V.2");
                                }
                                if(destroyed){
                                    break;
                                }
                            }
                        }
                    });

                    getData.start();
                }
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        destroyed = true;
        Intent mIntent = new Intent("receive-message");
        mIntent.putExtra("case-message", "end-oscilloscope");
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(mIntent);
        Log.d(TAG,"Sent destroy case-message");
        LocalBroadcastManager.getInstance(this.getContext()).unregisterReceiver(messageReceiver);
        super.onDestroyView();
        binding = null;
    }

    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        mHandler.removeCallbacks(mTimer1);
        super.onPause();
    }

    private void addData() {
        if(encodedVal != null){
            if(newPrintVal){
                int encodedInt = Integer.parseInt(encodedVal);
                double x = time;
                double y = -5.0 + encodedInt * (10.0/8192.0);
                DataPoint v = new DataPoint(x, y);
                mSeries.appendData(v,true,10000);
                time = time + 0.001;
                newPrintVal = false;
                Log.d(TAG, "Adding new data");
            }
        }

    }
}