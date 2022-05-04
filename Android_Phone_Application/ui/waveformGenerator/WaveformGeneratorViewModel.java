package com.example.blue2_1.ui.waveformGenerator;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class WaveformGeneratorViewModel extends ViewModel {

    private MutableLiveData<String> titleText;

    public WaveformGeneratorViewModel() {
        titleText = new MutableLiveData<>();
        titleText.setValue("");
    }

    public LiveData<String> get_textWaveformGenerator() {
        return titleText;
    }
}