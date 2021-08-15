package com.kpbird.nlsexample;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Entity;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.service.notification.NotificationListenerService;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.net.Socket;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends Activity {

    private Button btnCreateNotify;
    private SharedHelper sharedHelper;
    private TextView tip;
    private Button clipboardBtn;
    private Context context;
    private RadioGroup radioGroup;
    private Button connect;
    private Button close;
    private Socket client;
    private Button file;
    private Receiver receiver;
    public final static String broadcastName = "com.kpbird.nlsexample.RECEIVER";
    private int status = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();
        sharedHelper = new SharedHelper(context);
        checkAccessSetting();
        bindViews();
        initRadioButton();
        reBind();
        registerBroadcast();
    }

    private void registerBroadcast() {
        receiver = new Receiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(broadcastName);
        registerReceiver(receiver, intentFilter);
    }

    private void initRadioButton() {
        Map<String, ?> map = sharedHelper.list();
        for (Map.Entry<String, ?> entity : map.entrySet()) {
            final String alias = entity.getKey();
            final String ip = (String) entity.getValue();
            final RadioButton radioButton = new RadioButton(this);
            radioButton.setText(alias);
            radioButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SocketBuilder.SelectHost(ip);
                    if (!isWifiConnect()) {
                        sendBroadcast(-1);
                    } else {
                        connect.setEnabled(true);
                    }
                }
            });
            radioButton.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                            .setTitle("提示框")
                            .setMessage("确认删除" + alias)
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    radioGroup.removeView(radioButton);
                                    sharedHelper.remove(alias);
                                }
                            }).create();
                    dialog.show();
                    return true;
                }
            });
            radioGroup.addView(radioButton);
        }
    }

    private void gotoAddHostActivity() {
        Intent intent = new Intent(MainActivity.this, AddHostActivity.class);
        startActivity(intent);
    }

    private void checkAccessSetting() {
        if (!notificationListenerEnable()) {
            Toast.makeText(context, "请授予通知使用权", Toast.LENGTH_SHORT).show();
            gotoNotificationAccessSetting(context);
        }
    }

    private void bindViews() {
        Button insertBtn = (Button) findViewById(R.id.insert);
        btnCreateNotify = (Button) findViewById(R.id.btnCreateNotify);
        tip = (TextView) findViewById(R.id.tip);
        clipboardBtn = (Button) findViewById(R.id.clipboard);
        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        connect = (Button) findViewById(R.id.connect);
        close = (Button) findViewById(R.id.close);
        file = (Button) findViewById(R.id.file);
        connect.setEnabled(false);
        buttonEnabled(false);
        insertBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoAddHostActivity();
            }
        });
        clipboardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ClipboardActivity.class);
                startActivity(intent);
            }
        });
        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        client = SocketBuilder.builder();
                    }
                });
                thread.start();
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (client != null) {
                    tip.setText("状态：已连接");
                    buttonEnabled(true);
                } else {
                    Toast.makeText(context, "连接超时，请检查Host或服务端", Toast.LENGTH_SHORT).show();
                }
            }
        });
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SocketBuilder.close()) {
                    tip.setText("状态：未连接");
                    connect.setEnabled(true);
                    buttonEnabled(false);
                }
            }
        });
        file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, FileActivity.class);
                startActivity(intent);
            }
        });
    }

    private void buttonEnabled(boolean enabled) {
        btnCreateNotify.setEnabled(enabled);
        clipboardBtn.setEnabled(enabled);
        close.setEnabled(enabled);
        file.setEnabled(enabled);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(status == -1){
            tip.setText("状态：网络未打开");
            connect.setEnabled(false);
            buttonEnabled(false);
        }else if(status == 0){
            tip.setText("状态：未连接");
            connect.setEnabled(true);
            buttonEnabled(false);
        }else if(status == 1){
            tip.setText("状态：已连接");
            buttonEnabled(true);
        }else{
            tip.setText("状态：未连接");
            buttonEnabled(false);
        }
        radioGroup.removeAllViews();
        initRadioButton();
        client = SocketBuilder.client;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SocketBuilder.close();
        unBind();
        unregisterReceiver(receiver);
    }

    public void unBind() {
        NotificationService.notificationService.requestUnbind();
    }

    public void reBind() {
        ComponentName componentName = new ComponentName(this, NotificationService.class);
        NotificationListenerService.requestRebind(componentName);
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

    //判断wifi是否连接
    public boolean isWifiConnect() {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifiInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return mWifiInfo.isConnected();
    }

    class Receiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            status = intent.getIntExtra("status", 0);
        }
    }

    private void sendBroadcast(int status) {
        Intent intent = new Intent(broadcastName);
        intent.putExtra("status", status);
        sendBroadcast(intent);
    }
}