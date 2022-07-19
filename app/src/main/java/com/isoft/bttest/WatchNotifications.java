package com.isoft.bttest;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.io.IOException;

import java.util.Arrays;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class WatchNotifications extends Service {

    NotificationManagerCompat nmCompat;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;


    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            startForeground(intent);
            BTHandler bth = new BTHandler(this);
            final Integer[] i = {0};
            TimerTask tt = new TimerTask() {
                @Override
                public void run() {
                    i[0]++;
                    bth.sendMessage(String.valueOf(i[0]), true);
                }
            };
            Timer timer = new Timer("aaaaa");
            timer.schedule(tt, 0, 1000);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        nmCompat.cancel(101);
    }

    private void startForeground(Intent intent) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("Background Service", "Background Service", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "Background Service")
                .setSmallIcon(R.drawable.bt_not_con)
                .setContentTitle("Background service")
                .setOngoing(true)
                .setContentText("This notification is used to run the clock app on the background")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        Notification n = builder.build();
        nmCompat = NotificationManagerCompat.from(this);
        nmCompat.notify(101, n);



    }

}
