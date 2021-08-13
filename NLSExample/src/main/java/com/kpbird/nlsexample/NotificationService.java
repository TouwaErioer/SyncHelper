package com.kpbird.nlsexample;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.IOException;
import java.net.Socket;

public class NotificationService extends NotificationListenerService {

    private final String TAG = this.getClass().getSimpleName();
    private Socket client;

    @Override
    public void onCreate() {
        super.onCreate();
        buildSocket();
    }

    @Override
    public void onNotificationPosted(final StatusBarNotification sbn) {
        if (client == null) {
            buildSocket();
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
                    byte[] data = new byte[2048];
                    client.getInputStream().read(data);
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

    private void buildSocket() {
        Context context = getApplicationContext();
        final String ip = new SharedHelper(context).read();
        if (ip.equals("")) {
            Toast.makeText(context, "请输入IP", Toast.LENGTH_SHORT).show();
            return;
        }
        client = SocketBuilder.builder(ip);
        if (client == null) {
            send("success");
        } else {
            send("failure");
        }
    }

    private void send(String value) {
        Intent intent = new Intent();
        intent.putExtra("status", value);
        intent.setAction("location");
        sendBroadcast(intent);
    }

}
