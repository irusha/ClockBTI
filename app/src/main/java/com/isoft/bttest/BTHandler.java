package com.isoft.bttest;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

public class BTHandler {
    Context context;
    Set<BluetoothDevice> BTPairedDevices = null;
    public static BluetoothDevice btDevice = null;
    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    public static BluetoothSocket btSocket = null;
    boolean btConnected = false;

    public BTHandler(Context context){
        this.context = context;
    }

    public void connectToBTDevice(String Address) throws IOException {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "Bluetooth permission denied", Toast.LENGTH_SHORT).show();
            return;
        }
        BTPairedDevices = mBluetoothAdapter.getBondedDevices();
        if (BTPairedDevices.size() > 0){
            for (BluetoothDevice btDev: BTPairedDevices){
                if(btDev.getAddress().equals(Address)){
                    btDevice = btDev;
                    ((Activity)context).runOnUiThread(() -> Toast.makeText(context, "Connecting to: " + btDevice.getName(), Toast.LENGTH_SHORT).show());

                    System.out.println("Connection started");
                    connectToDevice(context);
                }
            }
        }
    }

    public void closeConnection() throws IOException {
        btSocket.close();
    }

    public boolean isAdapterEnabled() {
        return mBluetoothAdapter.isEnabled();
    }

    private void connectToDevice(Context context) throws IOException {
        final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        btSocket = btDevice.createRfcommSocketToServiceRecord(myUUID);
        System.out.println("Connecting to " + btDevice + " with " + myUUID);
        btSocket.connect();
        btConnected = true;
        //sendMessage("`e\n", true);
        System.out.println("Connected");

    }
    public void sendMessage(String message, boolean toast) {
        if (btSocket != null && btSocket.isConnected()) {

            try {
                btSocket.getOutputStream().write(message.getBytes());
            } catch (IOException e) {
                e.printStackTrace();

            }
        } else {
            if (toast) {
                Toast.makeText(context, "Please connect to the bluetooth device first", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
