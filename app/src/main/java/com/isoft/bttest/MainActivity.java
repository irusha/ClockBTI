package com.isoft.bttest;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {


    boolean alreadyChanged = false;
    boolean isOncePortClosed = false;
    boolean btConnected = false;
    String defaultSettings = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences btAddress = getSharedPreferences("btAddress", MODE_PRIVATE);
        ImageButton btBut = findViewById(R.id.btBut);


        BTHandler bth = new BTHandler(this);
        if(bth.isAdapterEnabled()){
            if (btAddress.contains("Address")) {
                Thread connectToBT = new Thread() {
                    public void run() {
                        try {
                            //Connect to bluetooth device
                            bth.connectToBTDevice(btAddress.getString("Address", ""));

                            //This boolean is to prevent the timer task automatically disconnect bluetooth
                            //even after it was disconnected
                            isOncePortClosed = false;
                            btConnected = true;
                            startService(new Intent(MainActivity.this, WatchNotifications.class));
                            runOnUiThread(() -> {
                                Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_SHORT).show();
                                btBut.setImageResource(R.drawable.bt_connected);
                                bth.sendMessage("`e\n", true);

                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                };
                connectToBT.start();

            }
        }

        btBut.setOnClickListener(v -> {
            if (!bth.isAdapterEnabled()) {
                Toast.makeText(this, "Please enable bluetooth before continuing", Toast.LENGTH_SHORT).show();
            }
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                System.out.println("Permission not granted");
                Toast.makeText(this, "Permission not granted", Toast.LENGTH_LONG).show();
                return;
            }
            Set<BluetoothDevice> BTPairedDevices = bth.mBluetoothAdapter.getBondedDevices();

            String[] selectedDevice = {""};
            if (BTPairedDevices.size() == 0) {
                if (bth.isAdapterEnabled()) {
                    Toast.makeText(this, "Please pair your device first", Toast.LENGTH_SHORT).show();
                }
            } else if (!(BTHandler.btSocket != null && BTHandler.btSocket.isConnected())) {
                ArrayList<String> devices = new ArrayList<>();

                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Permission not granted", Toast.LENGTH_SHORT).show());
                    return;
                }

                for (BluetoothDevice btDev : BTPairedDevices) {
                    devices.add(btDev.getName());
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Choose the device to pair");

                builder.setItems(arrayConverter(devices),
                        (dialog, which) -> {
                            defaultSettings = "";

                            selectedDevice[0] = arrayConverter(devices)[which];
                            for (BluetoothDevice btDev : BTPairedDevices) {
                                if (selectedDevice[0].equals(btDev.getName())) {
                                    SharedPreferences.Editor editBt = btAddress.edit();
                                    editBt.putString("Address", btDev.getAddress());
                                    editBt.apply();
                                    System.out.println(btDev);
                                    //Connect using a different thread
                                    Thread connectToBT = new Thread() {
                                        public void run() {
                                            try {
                                                //Connect to bluetooth device
                                                bth.connectToBTDevice(btAddress.getString("Address", ""));

                                                //This boolean is to prevent the timer task automatically disconnect bluetooth
                                                //even after it was disconnected
                                                isOncePortClosed = false;
                                                btConnected = true;
                                                runOnUiThread(() -> {
                                                    Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_SHORT).show();
                                                    btBut.setImageResource(R.drawable.bt_connected);
                                                    bth.sendMessage("`e\n", true);

                                                });
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    };
                                    connectToBT.start();
                                }
                            }
                        });

                AlertDialog dialog = builder.create();
                dialog.show();

            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Are you sure want to disconnect " + selectedDevice[0] + "?");
                builder.setPositiveButton("Ok", (dialog, which) -> {
                    try {
                        bth.closeConnection();
                        //stopService(new Intent(MainActivity.this, WatchNotifications.class));
                        btConnected = false;
                        btBut.setImageResource(R.drawable.bt_not_con);
                        alreadyChanged = false;
                        Toast.makeText(MainActivity.this, "Bluetooth device disconnected", Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                builder.setNegativeButton("Cancel", (dialog, which) -> {

                });

                builder.create();
                builder.show();
            }
        });



    }

    private static String[] arrayConverter(ArrayList<String> array) {
        String[] newArr = new String[array.size()];
        for (int i = 0; i < array.size(); i++) {
            newArr[i] = array.get(i);
        }
        return newArr;
    }

}