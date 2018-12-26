package com.hitsquadtechnologies.sifyconnect.ServerPrograms;

import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.util.Log;

import com.hsq.kw.packet.KeywestPacket;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

public class RouterService {

    public static String TAG = RouterService.class.getName();

    public static final int DEFAULT_PORT = 9181;
    public static final long MAX_SUBSCRIPTION_AGE = TimeUnit.MINUTES.toMillis(60);
    public static final long REQUEST_TIMEOUT = TimeUnit.SECONDS.toMillis(10);

    public static abstract class Callback<T> {
        public abstract void onSuccess(T t);

        public void onError(String msg, Exception e) {
            Log.e(TAG, msg, e);
        }
    }

    public static abstract class Subscription {

        public abstract void cancel();
    }

    public static abstract class Task<T> extends AsyncTask<Object, Void, T> {

        private CountDownTimer timer;
        private Callback<T> c;

        Task(Callback<T> c) {
            this.c = c;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            timer = new CountDownTimer(REQUEST_TIMEOUT, REQUEST_TIMEOUT) {
                public void onTick(long millisUntilFinished){}
                public void onFinish() {
                    Log.e(RouterService.class.getName(), "REQUEST TIMEOUT", null);
                    Task.this.cancel(true);
                    Task.this.onPostExecute(null);
                }
            };
            timer.start();
        }

        @Override
        protected void onPostExecute(T t) {
            super.onPostExecute(t);
            if (t == null) {
                this.c.onError("Request failed", null);
            } else {
                this.c.onSuccess(t);
            }
            this.timer.cancel();
        }
    }

    public static final RouterService INSTANCE = new RouterService();

    private String mIPAdress;
    private int mPort;
    private boolean serverFound;

    private RouterService() {}

    public void connectTo(String ipAddress, Callback<KeywestPacket> callback) {
        connectTo(ipAddress, DEFAULT_PORT, callback);
    }

    public void connectTo(String ipAddress, int port, final Callback<KeywestPacket> callback) {
        this.mIPAdress = ipAddress;
        this.mPort = port;
        KeywestPacket wirelessLinkPacket = new KeywestPacket((byte)1, (byte)1, (byte)2);
        sendRequest(wirelessLinkPacket, new RouterService.Callback<KeywestPacket>() {
            @Override
            public void onSuccess(final KeywestPacket packet) {
                serverFound = true;
                if (callback != null) {
                    callback.onSuccess(packet);
                }
            }

            @Override
            public void onError(String msg, Exception e) {
                serverFound = false;
                if (callback != null) {
                    callback.onError(msg, e);
                }
            }
        });
    }

    public boolean isServerFound() {
        return serverFound;
    }

    private AsyncTask send(final KeywestPacket keywestPacket, final Callback<KeywestPacket> callback) {
        final CountDownTimer timer;
        final Task task = new Task(callback) {
            private DatagramSocket mSocket;;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                try {
                    mSocket = new DatagramSocket();
                } catch (Exception e) {
                    throw new Error("Could not initiate data gram connection");
                }
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                if (mSocket != null) {
                    mSocket.close();
                }
            }

            @Override
            protected KeywestPacket doInBackground(Object... objects) {
                KeywestPacket receivedPacket = null;
                Socket socket = null;
                try {
                    InetAddress address = InetAddress.getByName(mIPAdress);
                    byte[] bytes = keywestPacket.toByteArray();
                    socket = new Socket(address,mPort);
                    DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                    //ByteArrayOutputStream bos = new ByteArrayOutputStream(socket.getOutputStream());
                    DataInputStream dis = new DataInputStream(socket.getInputStream());

                    //DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, mPort);
                    dos.write(bytes);
                    byte[] buf = new byte[1307];
                    dis.read(buf);
                    //packet =s new DatagramPacket(buf, buf.length);
                    //mSocket.receive(packet);
                    receivedPacket = new KeywestPacket(buf);

                } catch (Exception e) {
                    Log.e(RouterService.class.getName(), "communication failed", e);
                } finally {
                    if (socket != null) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                return  receivedPacket;
            }
        };
        return task;
    }

    public void sendRequest(final KeywestPacket keywestPacket, final Callback<KeywestPacket> callback) {
        send(keywestPacket, callback).execute();
    }

    public Subscription observe(final KeywestPacket keywestPacket, final Callback<KeywestPacket> callback) {
        final CountDownTimer timer = new CountDownTimer(MAX_SUBSCRIPTION_AGE, 3000) {
            public void onTick(long millisUntilFinished){
                sendRequest(keywestPacket, callback);
            }
            public void onFinish() {
                sendRequest(keywestPacket, callback);
            }
        }.start();
        return new Subscription() {
            @Override
            public void cancel() {
                timer.cancel();
            }
        };
    }

}
