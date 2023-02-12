package com.arthaiirgames.usbmidiandroid;

import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.concurrent.atomic.AtomicBoolean;

public class MidiInputDevice {
    private final MidiThread _midiThread;
    private final UsbEndpoint _endpoint;
    private final UsbDeviceConnection _usbDeviceConnection;
    private IMidiCallback _midiCallback;

    public MidiInputDevice(@NonNull UsbEndpoint endpoint, @NonNull UsbDeviceConnection usbDeviceConnection, @NonNull UsbInterface usbInterface, IMidiCallback callback)
    {
        Log.i("NEW THREAD", "new MIDI INPUT DEVICE");
        _midiThread = new MidiThread();
        _endpoint = endpoint;
        _midiCallback = callback;
        _usbDeviceConnection = usbDeviceConnection;
        _usbDeviceConnection.claimInterface(usbInterface, true);
        _midiThread.start();
    }

    public void stopThread()
    {
        _midiThread.stopThread();
    }

    private class MidiThread extends Thread{

        private AtomicBoolean _running = new AtomicBoolean(true);

        public MidiThread()
        {
        }

        public void stopThread()
        {
            _running.set(false);
        }

        @Override
        public void run()
        {
            byte[] receiveBuffer = new byte[_endpoint.getMaxPacketSize()];
            while (_running.get())
            {
                int size = _usbDeviceConnection.bulkTransfer(_endpoint, receiveBuffer, _endpoint.getMaxPacketSize(), 10);
                if (size <= 0)
                    continue;
                for (int i = 0; i < size; i += 4) {
                    int CIN = receiveBuffer[i] & 0xf;
                    int byte2 = receiveBuffer[i + 2] & 0xff;
                    int byte3 = receiveBuffer[i + 3] & 0xff;
                    _midiCallback.RawMidi((byte)CIN, (byte)byte2, (byte)byte3);
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
            Log.i("MidiThread", "Successfully stopped thread");
        }
    }
}
