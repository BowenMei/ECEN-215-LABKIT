package com.example.blue2_1.ui.ohmmeter;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class OhmmeterViewModel extends ViewModel {

    private MutableLiveData<String> titleText;
    private MutableLiveData<String> resistanceNumber;

    public OhmmeterViewModel() {
        titleText = new MutableLiveData<>();
        titleText.setValue("");

        resistanceNumber = new MutableLiveData<>();
        resistanceNumber.setValue("0.0 Ω");
    }

    public void setResistance(double value){
        resistanceNumber.setValue(String.valueOf(value));
    }

    public LiveData<String> get_textOhmmeter() {
        return titleText;
    }
    public LiveData<String> get_text_resistanceNumber() { return resistanceNumber; }
}