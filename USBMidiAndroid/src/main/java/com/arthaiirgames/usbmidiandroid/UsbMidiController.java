package com.arthaiirgames.usbmidiandroid;

import static android.app.PendingIntent.FLAG_MUTABLE;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;

public class UsbMidiController {

    private static final UsbMidiController _instance = new UsbMidiController();

    public static UsbMidiController getInstance() {
        return (_instance);
    }

    private UsbManager _usbManager;
    private Context _context;
    private final String USB_PERMISSION_ACTION = "com.arthaiirgames.unitymidiandroid.USB_PERMISSION_GRANTED_ACTION";
    private IMidiCallback _midiCallback = null;
    private ArrayList<MidiInputDevice> _interfaces = null;

    private UsbMidiController() {
    }

    public void ctor(IMidiCallback callback, @NonNull Activity activity) {
        _context = activity.getApplicationContext();
        _midiCallback = callback;
        registerMidiDevices();
    }

    public void registerMidiDevices() {
        _usbManager = (UsbManager) _context.getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> map = _usbManager.getDeviceList();
        for (UsbDevice usbDevice : map.values()) {
            try {
                int flag;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    flag = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE;

                } else {
                    flag = PendingIntent.FLAG_UPDATE_CURRENT;
                }
                PendingIntent permissionIntent = PendingIntent.getBroadcast(_context, 0, new Intent(USB_PERMISSION_ACTION), flag);
                IntentFilter filter = new IntentFilter(USB_PERMISSION_ACTION);
                filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
                filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
                _context.registerReceiver(new UsbReceiver(usbDevice), filter);
                _usbManager.requestPermission(usbDevice, permissionIntent);
            } catch (Exception e) {
                String message = e.getMessage();
                if (message == null)
                    Log.e("UsbMidiController", e.toString());
                else Log.e("UsbMidiController", e.getMessage());
            }
        }
    }

    @Nullable
    private ArrayList<MidiInputDevice> getAllMidiInterfaces(@NonNull UsbDevice usbDevice) {
        ArrayList<MidiInputDevice> devices = new ArrayList<>();
        int interfaceCount = usbDevice.getInterfaceCount();
        UsbDeviceConnection usbDeviceConnection = _usbManager.openDevice(usbDevice);
        if (!_usbManager.hasPermission(usbDevice))
            return null;
        for (int i = 0; i < interfaceCount; i++) {
            UsbInterface usbInterface = usbDevice.getInterface(i);
            UsbEndpoint endpoint = getEndpoint(usbInterface);
            if (endpoint != null) {
                if (usbDeviceConnection == null)
                    continue;
                devices.add(new MidiInputDevice(endpoint, usbDeviceConnection, usbInterface, _midiCallback));
            }
        }
        return devices;
    }

    UsbEndpoint getEndpoint(@NonNull UsbInterface usbInterface) {
        if (usbInterface.getInterfaceClass() == UsbConstants.USB_CLASS_AUDIO && usbInterface.getInterfaceSubclass() == UsbConstants.USB_CLASS_HID) {
            for (int i = 0; i < usbInterface.getEndpointCount(); i++) {
                UsbEndpoint usbEndpoint = usbInterface.getEndpoint(i);
                if (usbEndpoint.getDirection() == UsbConstants.USB_DIR_IN)
                    return usbEndpoint;
            }
        }
        return null;
    }

    private class UsbReceiver extends BroadcastReceiver {
        private final UsbDevice _usbDevice;

        public UsbReceiver(UsbDevice usbDevice) {
            _usbDevice = usbDevice;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                if (USB_PERMISSION_ACTION.equals(action)) {
                    _interfaces = getAllMidiInterfaces(_usbDevice);
                }
            }
            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                _midiCallback.DeviceDetached(_usbDevice.getDeviceName());
            }
            if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                _midiCallback.DeviceAttached(_usbDevice.getDeviceName());
                registerMidiDevices();
            }
        }
    }
}
