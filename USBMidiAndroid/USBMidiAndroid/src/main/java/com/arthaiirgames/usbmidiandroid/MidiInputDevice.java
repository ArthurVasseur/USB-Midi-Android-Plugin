package com.arthaiirgames.usbmidiandroid;

import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.util.Log;

import androidx.annotation.NonNull;

public class MidiInputDevice {
    private final MidiThread _midiThread;
    private final UsbEndpoint _endpoint;
    private final UsbDeviceConnection _usbDeviceConnection;
    private IMidiCallback _midiCallback;

    public MidiInputDevice(@NonNull UsbEndpoint endpoint, @NonNull UsbDeviceConnection usbDeviceConnection, @NonNull UsbInterface usbInterface, IMidiCallback callback)
    {
        Log.e("NEW THREAD", "new MIDI INPUT DEVICE");
        _midiThread = new MidiThread();
        _endpoint = endpoint;
        _midiCallback = callback;
        _usbDeviceConnection = usbDeviceConnection;
        _usbDeviceConnection.claimInterface(usbInterface, true);
        _midiThread.start();
    }
    private class MidiThread extends Thread{

        public MidiThread()
        {

        }

        @Override
        public void run()
        {
            byte[] receiveBuffer = new byte[_endpoint.getMaxPacketSize()];
            while (true)
            {
                int size = _usbDeviceConnection.bulkTransfer(_endpoint, receiveBuffer, _endpoint.getMaxPacketSize(), 10);
                if (size <= 0)
                    continue;
                for (int i = 0; i < size; i += 4) {
                    int CIN = receiveBuffer[i] & 0xf;
                    int byte2 = receiveBuffer[i + 2] & 0xff;
                    int byte3 = receiveBuffer[i + 3] & 0xff;
                    switch (CIN)
                    {
                        case 0x8:
                            _midiCallback.NoteOff(byte2);
                            break;
                        case 0x9:
                            if(byte3 == 0x00) {
                                _midiCallback.NoteOff(byte2);
                            }
                            else {
                                _midiCallback.NoteOn(byte2, byte3);
                            }
                            break;
                    }
                }
            }
        }
    }
}
