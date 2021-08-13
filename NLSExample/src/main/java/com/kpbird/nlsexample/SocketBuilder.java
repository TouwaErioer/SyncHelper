package com.kpbird.nlsexample;

import java.io.IOException;
import java.net.Socket;

public class SocketBuilder {

    private static Socket client;

    private SocketBuilder() {

    }

    public static Socket builder(final String ip) {
        if (client == null) {
            synchronized (SocketBuilder.class) {
                if (client == null) {
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
        }
        return client;
    }

    public static void close() {
        if (client != null) {
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            client = null;
        }
    }
}