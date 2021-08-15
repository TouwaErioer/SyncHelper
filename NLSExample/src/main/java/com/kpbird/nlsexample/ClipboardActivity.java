package com.kpbird.nlsexample;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import java.net.Socket;

public class ClipboardActivity extends AppCompatActivity {

    private Socket client;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clipboard);
        context = getApplicationContext();
        client = SocketBuilder.client;
        bindView();
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
                        if (client == null) {
                            client = SocketBuilder.builder();
                        }
                        try {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("Type", "Clipboard");
                            jsonObject.put("Content", contentEditText.getText().toString());
                            client.getOutputStream().write(jsonObject.toString().getBytes());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
                Toast.makeText(context, "发送成功", Toast.LENGTH_SHORT).show();
            }
        });
    }
}