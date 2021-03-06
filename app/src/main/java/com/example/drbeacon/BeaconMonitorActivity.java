package com.example.drbeacon;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.example.utils.HTTPGet;

import java.util.List;
import java.util.UUID;

public class BeaconMonitorActivity extends Application {

    private BeaconManager beaconManager;

    @Override
    public void onCreate() {
        super.onCreate();

        beaconManager = new BeaconManager(getApplicationContext());
        beaconManager.setMonitoringListener(new BeaconManager.MonitoringListener() {
            @Override
            public void onEnteredRegion(Region region, List<Beacon> list) {
                showNotification(
                        "Check in to your appointment",
                        "Please wait until your are called");

                Intent intent = new Intent(BeaconMonitorActivity.this,
                        CheckInActivity.class);
                startActivity(intent);
                try {
                    Thread.sleep(5000);
                }
                catch (InterruptedException e) {}

                intent = new Intent(BeaconMonitorActivity.this,
                        DoctorScreen.class);
            }
            @Override
            public void onExitedRegion(Region region) {
                showNotification(
                        "Thank you",
                        "Hope you have a happy and healthy day!");

                new HTTPGet().execute("http://10.192.118.246:3000/api/dequeue");
            }
        });
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startMonitoring(new Region("monitored region",
                        UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"), 15230, 7552));
            }
        });
    }

    public void showNotification(String title, String message) {
        Intent notifyIntent = new Intent(this, MainActivity.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        notifyIntent.putExtra("clinic", "Gaudi Medical Clinic");
        notifyIntent.putExtra("doctor", "Doctor Jane Beacon");
        notifyIntent.putExtra("location", "Carrer de Mallorca, 401, 08013 Barcelona");

        PendingIntent pendingIntent = PendingIntent.getActivities(this, 0,
                new Intent[]{notifyIntent}, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(android.R.drawable.star_off)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build();
        notification.defaults |= Notification.DEFAULT_SOUND;
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);
    }


}
