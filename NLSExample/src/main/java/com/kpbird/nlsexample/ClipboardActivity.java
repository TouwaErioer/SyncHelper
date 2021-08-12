package com.kpbird.nlsexample;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONObject;

import java.io.IOException;
import java.net.Socket;

public class ClipboardActivity extends AppCompatActivity {

    private Socket client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clipboard);
        buildSocket();
        bindView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (client != null) {
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void bindView() {
        final EditText contentEditText = (EditText) findViewById(R.id.content);
        Button sendBtn = (Button) findViewById(R.id.send);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("Type", "Clipboard");
                            jsonObject.put("Content", contentEditText.getText().toString());
                            client.getOutputStream().write(jsonObject.toString().getBytes());
                            byte[] data = new byte[2048];
                            client.getInputStream().read(data);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }).start();
            }
        });
    }

    public void buildSocket() {
        Context context = getApplicationContext();
        final String ip = new SharedHelper(context).read();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    client = new Socket(ip, 1208);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}