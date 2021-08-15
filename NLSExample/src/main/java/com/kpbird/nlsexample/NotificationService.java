package com.kpbird.nlsexample;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import org.json.JSONObject;

import java.net.Socket;

public class NotificationService extends NotificationListenerService {

    private final String TAG = this.getClass().getSimpleName();
    private Socket client;
    public static NotificationService notificationService;

    @Override
    public void onCreate() {
        super.onCreate();
        client = SocketBuilder.client;
        notificationService = this;
    }

    @Override
    public void onNotificationPosted(final StatusBarNotification sbn) {
        if (client == null) {
            client = SocketBuilder.builder();
        }
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Bundle extras = sbn.getNotification().extras;
                    String title = extras.getString(Notification.EXTRA_TITLE);
                    String content = extras.getString(Notification.EXTRA_TEXT);
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("Type", "Notification");
                    jsonObject.put("Title", title);
                    jsonObject.put("Content", content);
                    client.getOutputStream().write(jsonObject.toString().getBytes());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.i(TAG, "ID :" + sbn.getId() + "\t" + sbn.getNotification().tickerText + "\t" + sbn.getPackageName());
    }
}