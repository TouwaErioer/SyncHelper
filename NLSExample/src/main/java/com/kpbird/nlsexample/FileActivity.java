package com.kpbird.nlsexample;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.Callable;

public class FileActivity extends AppCompatActivity {

    private Socket client;
    private FileInputStream fis;
    private DataOutputStream dos;
    private static final String qqFilePath = "/sdcard/Android/data/com.tencent.mobileqq/Tencent/QQfile_recv/";
    private static final String weixinFilePath = "/sdcard/Download/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file);
        bindView();
        client = SocketBuilder.client;
    }

    private void bindView() {
        Button selectFile = (Button) findViewById(R.id.selectFile);
        Button sendByQQ = (Button) findViewById(R.id.sendByQQ);
        Button sendByWeiXin = (Button) findViewById(R.id.sendByWeiXin);
        selectFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickFile();
            }
        });
        sendByQQ.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendFile(getFilesAllName(qqFilePath));

            }
        });
        sendByWeiXin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendFile(getFilesAllName(weixinFilePath));
            }
        });
    }

    private void sendBroadcast(int status) {
        Intent intent = new Intent(MainActivity.broadcastName);
        intent.putExtra("status", status);
        sendBroadcast(intent);
    }

    public static String getFilesAllName(String path) {
        File file = new File(path);
        File[] files = file.listFiles();
        if (files == null) {
            return null;
        }
        long max = 0;
        File temp = null;
        for (File f : files) {
            if (f.lastModified() > max) {
                temp = f;
            }
        }
        assert temp != null;
        return temp.getPath();
    }

    public void pickFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        this.startActivityForResult(intent, 1);
    }

    private void sendFile(final String path) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (client == null) {
                    client = SocketBuilder.builder();
                }
                File file = new File(path);
                try {
                    fis = new FileInputStream(file);
                    dos = new DataOutputStream(client.getOutputStream());
                    byte[] bytes = new byte[1024];
                    int length = 0;
                    while ((length = fis.read(bytes, 0, bytes.length)) != -1) {
                        dos.write(bytes, 0, length);
                        dos.flush();
                    }
                    sendToast("发送成功");
                    //todo 发送完毕关闭连接
                    sendBroadcast(0);
                } catch (IOException e) {
                    e.printStackTrace();
                    sendToast("发送失败");
                } finally {
                    try {
                        fis.close();
                        dos.close();
                        client.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            String path;
            Uri uri = data.getData();
            assert uri != null;
            if ("file".equalsIgnoreCase(uri.getScheme())) {//使用第三方应用打开
                path = uri.getPath();
            }
            //4.4以后
            path = getPath(this, uri);
            sendFile(path);
        }
    }

    private void sendToast(String content){
        Looper.prepare();
        Toast.makeText(getApplicationContext(), content, Toast.LENGTH_SHORT).show();
        Looper.loop();
    }

    /**
     * 专为Android4.4设计的从Uri获取文件绝对路径，以前的方法已不好使
     */
    @SuppressLint("NewApi")
    public String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public String getDataColumn(Context context, Uri uri, String selection,
                                String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
}