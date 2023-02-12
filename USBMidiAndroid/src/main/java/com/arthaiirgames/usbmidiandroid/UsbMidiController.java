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
    private UsbReceiver _receiver = null;
    private HashMap<UsbDevice, ArrayList<MidiInputDevice>> _interfacesByUsbDevice = new HashMap<>();

    private UsbMidiController() {
    }

    public void ctor(IMidiCallback callback, @NonNull Activity activity) {
        _context = activity.getApplicationContext();
        _midiCallback = callback;
        _receiver = new UsbReceiver();
        registerUsbReceiver();
        discoverExistingDevices();
    }

    private void registerUsbReceiver() {
        _usbManager = (UsbManager) _context.getSystemService(Context.USB_SERVICE);
        try {
            PendingIntent permissionIntent = createUsbPermissionIntent();
            IntentFilter filter = new IntentFilter(USB_PERMISSION_ACTION);
            filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
            filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
            UsbReceiver receiver = new UsbReceiver();
            _context.registerReceiver(receiver, filter);
        } catch (Exception e) {
            String message = e.getMessage();
            if (message == null)
                Log.e("UsbMidiController", e.toString());
            else Log.e("UsbMidiController", e.getMessage());
        }
    }

    // Receiver will detect new devices added but on start-up we must handle existing devices
    private void discoverExistingDevices() {
        HashMap<String, UsbDevice> map = _usbManager.getDeviceList();
        for (UsbDevice usbDevice : map.values()) {
            requestPermissionForDevice(usbDevice);
        }
    }

    private void requestPermissionForDevice(UsbDevice usbDevice) {
        try {
            PendingIntent permissionIntent = createUsbPermissionIntent();
            _usbManager.requestPermission(usbDevice, permissionIntent);
        } catch (Exception e) {
            String message = e.getMessage();
            if (message == null)
                Log.e("UsbMidiController", e.toString());
            else Log.e("UsbMidiController", e.getMessage());
        }
    }

    private PendingIntent createUsbPermissionIntent() {
        int flag;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            flag = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE;
        } else {
            flag = PendingIntent.FLAG_UPDATE_CURRENT;
        }
        return PendingIntent.getBroadcast(_context, 0, new Intent(USB_PERMISSION_ACTION), flag);
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

    private void addMidiInputDevices(UsbDevice device, ArrayList<MidiInputDevice> interfaces) {
        _interfacesByUsbDevice.put(device, interfaces);
    }

    private void removeMidiInputDevices(UsbDevice device) {
        ArrayList<MidiInputDevice> interfaces = _interfacesByUsbDevice.remove(device);
        if (interfaces != null)
        {
            for (int i = 0; i < interfaces.size(); i++) {
                interfaces.get(i).stopThread();
            }
        }
    }

    private class UsbReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            UsbDevice usbDevice = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            String action = intent.getAction();
            if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                if (USB_PERMISSION_ACTION.equals(action)) {
                    // Get MIDI interfaces (if any)
                    ArrayList<MidiInputDevice> interfaces = getAllMidiInterfaces(usbDevice);
                    if (interfaces.size() > 0)
                    {
                        removeMidiInputDevices(usbDevice);
                        addMidiInputDevices(usbDevice, interfaces);
                        _midiCallback.DeviceAttached(usbDevice.getDeviceName());
                    }
                }
            }
            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                removeMidiInputDevices(usbDevice);
                _midiCallback.DeviceDetached(usbDevice.getDeviceName());
            }
            if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                requestPermissionForDevice(usbDevice);
            }
        }
    }
}
