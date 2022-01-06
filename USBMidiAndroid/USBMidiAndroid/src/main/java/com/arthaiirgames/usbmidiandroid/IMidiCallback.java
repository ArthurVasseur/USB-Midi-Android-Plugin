package com.arthaiirgames.usbmidiandroid;

public interface IMidiCallback {
    void NoteOn(int note, int velocity);
    void NoteOff(int note);
    void DeviceAttached(String name);
    void DeviceDetached(String name);
}
