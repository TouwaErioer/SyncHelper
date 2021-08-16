package com.kpbird.nlsexample;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class AddHostActivity extends AppCompatActivity {

    private EditText aliasEditText;
    private EditText ipEditText;
    private SharedHelper sharedHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_host);
        Context context = getApplicationContext();
        sharedHelper = new SharedHelper(context, "data");
        bindView();
    }

    private void bindView() {
        aliasEditText = (EditText) findViewById(R.id.alias);
        ipEditText = (EditText) findViewById(R.id.host);
        Button saveBtn = (Button) findViewById(R.id.save);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ip = ipEditText.getText().toString();
                String alias = aliasEditText.getText().toString();
                if (sharedHelper.save(alias, ip)) {
                    Intent intent = new Intent(AddHostActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            }
        });
    }
}