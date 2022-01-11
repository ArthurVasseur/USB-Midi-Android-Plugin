package com.arthaiirgames.exampleandroidapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.arthaiirgames.usbmidiandroid.UsbMidiController;

public class MainActivity extends AppCompatActivity {

    private MyCallback _myCallback;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        _myCallback = new MyCallback(findViewById(R.id.textViewMidiLog));
        UsbMidiController.getInstance().ctor(_myCallback, this);
    }
}