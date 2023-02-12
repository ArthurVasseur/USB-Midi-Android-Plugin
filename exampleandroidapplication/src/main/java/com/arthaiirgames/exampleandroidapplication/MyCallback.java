package com.arthaiirgames.exampleandroidapplication;

import android.util.Log;
import android.widget.TextView;

import com.arthaiirgames.usbmidiandroid.IMidiCallback;

public class MyCallback implements IMidiCallback {

    TextView _textView = null;
    public MyCallback(TextView textView)
    {
        _textView = textView;
    }

    @Override
    public void RawMidi(byte command, byte data1, byte data2) {
        _textView.append(" MIDI command : " + command + ", " + data1 + ", " + data2 + System.lineSeparator());
    }

    @Override
    public void NoteOn(int note, int velocity) {
        _textView.append(" Note : " + note + " velocity : " + velocity + System.lineSeparator());
    }

    @Override
    public void NoteOff(int note) {
        _textView.append(" Note : " + note + System.lineSeparator());
    }

    @Override
    public void DeviceAttached(String name) {
        _textView.append(" DeviceAttached Name : " + name + System.lineSeparator());
    }

    @Override
    public void DeviceDetached(String name) {
        _textView.append(" DeviceDetached Name : " + name + System.lineSeparator());
    }
}
