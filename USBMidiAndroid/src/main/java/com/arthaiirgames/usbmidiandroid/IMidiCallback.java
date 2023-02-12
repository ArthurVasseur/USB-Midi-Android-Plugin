package com.arthaiirgames.usbmidiandroid;

public interface IMidiCallback {
    void DeviceAttached(String name);
    void DeviceDetached(String name);
    void RawMidi(byte command, byte data1, byte data2);
    void NoteOn(int note, int velocity);
    void NoteOff(int note);
}
