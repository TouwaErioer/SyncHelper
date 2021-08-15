package com.kpbird.nlsexample;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class SocketBuilder {

    public static Socket client;
    public static String ip;

    private SocketBuilder() {

    }

    public static void SelectHost(String ip) {
        SocketBuilder.ip = ip;
        close();
    }

    public static Socket builder() {
        if (client == null) {
            synchronized (SocketBuilder.class) {
                if (client == null) {
                    try {
                        client = new Socket();
                        client.connect(new InetSocketAddress(ip, 1208), 2 * 1000);
                        byte[] buf = new byte[2048];
                        int input = client.getInputStream().read(buf);
                        String result = new String(buf, 0, input);
                        if (result.equals("reject")) {
                            close();
                        }
                    } catch (SocketTimeoutException e) {
                        return null;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return client;
    }

    public static boolean close() {
        if (client != null) {
            try {
                client.close();
                client = null;
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}