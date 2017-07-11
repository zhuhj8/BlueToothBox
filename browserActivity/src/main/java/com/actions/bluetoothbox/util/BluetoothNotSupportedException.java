package com.actions.bluetoothbox.util;

public class BluetoothNotSupportedException extends Exception {
    public BluetoothNotSupportedException() {
        super("Bluetooth is not supported on this device!");
    }
}