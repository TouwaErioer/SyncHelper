package com.kpbird.nlsexample;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import java.util.Objects;
import java.util.regex.Pattern;

public class SharedHelper {

    private Context context;
    private static final String regex = "((?:(?:25[0-5]|2[0-4]\\d|[01]?\\d?\\d)\\.){3}(?:25[0-5]|2[0-4]\\d|[01]?\\d?\\d))";

    public SharedHelper(Context context) {
        this.context = context;
    }


    //定义一个保存数据的方法
    public boolean save(String ip) {
        if (ip.equals("")) {
            Toast.makeText(context, "IP不能为空", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!Pattern.matches(regex, ip)) {
            Toast.makeText(context, "IP不合法", Toast.LENGTH_SHORT).show();
            return false;
        }
        SharedPreferences sp = context.getSharedPreferences("data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("ip", ip);
        editor.apply();
        Toast.makeText(context, "IP已保存", Toast.LENGTH_SHORT).show();
        return true;
    }

    //定义一个读取SP文件的方法
    public String read() {
        SharedPreferences sp = context.getSharedPreferences("data", Context.MODE_PRIVATE);
        return Objects.requireNonNull(sp.getString("ip", ""));
    }
}