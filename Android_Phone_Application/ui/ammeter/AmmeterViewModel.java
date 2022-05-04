package com.example.blue2_1.ui.ammeter;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AmmeterViewModel extends ViewModel {

    private MutableLiveData<String> titleText;
    private MutableLiveData<String> currentNumber;

    public AmmeterViewModel() {
        titleText = new MutableLiveData<>();
        titleText.setValue("");

        currentNumber = new MutableLiveData<>();
        currentNumber.setValue("0.0 A");
    }

    public void setCurrent(double value){
        currentNumber.setValue(String.valueOf(value));
    }

    public LiveData<String> get_textAmmeter() {
        return titleText;
    }
    public LiveData<String> get_text_currentNumber() {return currentNumber; }
}