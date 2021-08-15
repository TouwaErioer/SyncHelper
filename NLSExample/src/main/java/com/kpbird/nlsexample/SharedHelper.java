package com.kpbird.nlsexample;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

public class SharedHelper {

    private Context context;
    private static final String regex = "((?:(?:25[0-5]|2[0-4]\\d|[01]?\\d?\\d)\\.){3}(?:25[0-5]|2[0-4]\\d|[01]?\\d?\\d))";
    private SharedPreferences sp;

    public SharedHelper(Context context) {
        this.context = context;
        sp = context.getSharedPreferences("data", Context.MODE_PRIVATE);
    }


    //定义一个保存数据的方法
    public boolean save(String alias, String ip) {
        if (alias.equals("") || ip.equals("")) {
            Toast.makeText(context, "IP或别名不能为空", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!Pattern.matches(regex, ip)) {
            Toast.makeText(context, "IP不合法", Toast.LENGTH_SHORT).show();
            return false;
        }
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(alias, ip);
        editor.apply();
        Toast.makeText(context, "已保存", Toast.LENGTH_SHORT).show();
        return true;
    }

    //定义一个读取SP文件的方法
    public String read(String alias) {
        ;
        return Objects.requireNonNull(sp.getString(alias, ""));
    }

    public Map<String, ?> list() {
        return sp.getAll();
    }

    public void remove(String alias) {
        SharedPreferences.Editor editor = sp.edit();
        editor.remove(alias);
        editor.apply();
        Toast.makeText(context, "已删除", Toast.LENGTH_SHORT).show();
    }
}