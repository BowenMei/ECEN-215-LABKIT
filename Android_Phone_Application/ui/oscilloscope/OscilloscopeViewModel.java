package com.example.blue2_1.ui.oscilloscope;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class OscilloscopeViewModel extends ViewModel {

    private MutableLiveData<String> titleText;

    public OscilloscopeViewModel() {
        titleText = new MutableLiveData<>();
        titleText.setValue("");
    }

    public LiveData<String> get_textOscilloscope() {
        return titleText;
    }
}