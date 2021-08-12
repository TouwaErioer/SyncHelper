package com.kpbird.nlsexample;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Map;
import java.util.Objects;

public class MainActivity extends Activity {

    private EditText ipEditText;
    private Button btnCreateNotify;
    private SharedHelper sharedHelper;
    private LocationReceiver locationReceiver;
    private TextView tip;
    private Button clipboardBtn;
    private TextView help;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Context context = getApplicationContext();
        if (!notificationListenerEnable()) {
            Toast.makeText(context, "请授予通知使用权", Toast.LENGTH_SHORT).show();
            gotoNotificationAccessSetting(context);
        }
        sharedHelper = new SharedHelper(context);
        bindViews();
        String ip = sharedHelper.read();
        ipEditText.setText(ip);
        if (ip.equals("")) {
            btnCreateNotify.setEnabled(false);
            clipboardBtn.setEnabled(false);
        } else {
            help.setText("");
        }
        locationReceiver = new LocationReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("location");
        registerReceiver(locationReceiver, filter);
    }

    private void bindViews() {
        ipEditText = (EditText) findViewById(R.id.host);
        Button saveBtn = (Button) findViewById(R.id.save);
        btnCreateNotify = (Button) findViewById(R.id.btnCreateNotify);
        tip = (TextView) findViewById(R.id.tip);
        clipboardBtn = (Button) findViewById(R.id.clipboard);
        help = (TextView) findViewById(R.id.help);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ip = ipEditText.getText().toString();
                if (sharedHelper.save(ip)) {
                    btnCreateNotify.setEnabled(true);
                    clipboardBtn.setEnabled(true);
                    help.setText("");
                }
            }
        });
        clipboardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ClipboardActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(locationReceiver);
    }

    public void buttonClicked(View v) {
        if (v.getId() == R.id.btnCreateNotify) {
            if (!notificationListenerEnable()) {
                Toast.makeText(getApplicationContext(), "没有授予通知使用权", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                NotificationManager nManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                NotificationCompat.Builder notification = new NotificationCompat.Builder(this);
                notification.setContentTitle("My Notification");
                notification.setContentText("Notification Listener Service Example");
                notification.setTicker("test");
                notification.setSmallIcon(R.drawable.ic_launcher);
                notification.setAutoCancel(true);
                nManager.notify((int) System.currentTimeMillis(), notification.build());
            }
        }
    }

    public class LocationReceiver extends BroadcastReceiver {
        //必须要重载的方法，用来监听是否有广播发送
        @Override
        public void onReceive(Context context, Intent intent) {
            String status = (String) Objects.requireNonNull(intent.getExtras()).get("status");
            assert status != null;
            if (status.equals("success")) {
                tip.setText("状态：已连接");
            }
        }
    }


    //判断是否拥有通知使用权
    private boolean notificationListenerEnable() {
        boolean enable = false;
        String packageName = getPackageName();
        String flat = Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners");
        if (flat != null) {
            enable = flat.contains(packageName);
        }
        return enable;
    }

    //跳转系统设置里的通知使用权页面
    private boolean gotoNotificationAccessSetting(Context context) {
        try {
            Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            return true;
        } catch (ActivityNotFoundException e) {
            try {
                Intent intent = new Intent();
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                ComponentName cn = new ComponentName("com.android.settings", "com.android.settings.Settings$NotificationAccessSettingsActivity");
                intent.setComponent(cn);
                intent.putExtra(":settings:show_fragment", "NotificationAccessSettings");
                context.startActivity(intent);
                return true;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return false;
        }
    }
}