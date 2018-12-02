package com.hitsquadtechnologies.sifyconnect.ServerPrograms;

import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.util.Log;

import com.hsq.kw.packet.KeywestPacket;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

public class RouterService {

    public static String TAG = RouterService.class.getName();

    public static final int DEFAULT_PORT = 9181;
    public static final long MAX_SUBSCRIPTION_AGE = TimeUnit.MINUTES.toMillis(60);

    public static abstract class Callback<T> {
        public abstract void onSuccess(T t);

        public void onError(String msg, Exception e) {
            Log.e(TAG, msg, e);
        }
    }

    public static abstract class Subscription {

        public abstract void cancel();
    }

    public static final RouterService INSTANCE = new RouterService();

    private String mIPAdress;
    private int mPort;

    private RouterService() {}

    public void connectTo(String ipAddress) {
        connectTo(ipAddress, DEFAULT_PORT);
    }

    public void connectTo(String ipAddress, int port) {
        this.mIPAdress = ipAddress;
        this.mPort = port;
    }

    private AsyncTask send(final KeywestPacket keywestPacket, final Callback<KeywestPacket> callback) {
        return new AsyncTask<Object, Void, Void>() {
            @Override
            protected Void doInBackground(Object... objects) {
                DatagramSocket socket = null;
                try {
                    socket = new DatagramSocket();
                    InetAddress address = InetAddress.getByName(mIPAdress);
                    byte[] bytes = keywestPacket.toByteArray();
                    DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, mPort);
                    socket.send(packet);
                    byte[] buf = new byte[1307];
                    packet = new DatagramPacket(buf, buf.length);
                    socket.receive(packet);
                    KeywestPacket receivedPacket = new KeywestPacket(packet);
                    callback.onSuccess(receivedPacket);
                } catch (Exception e) {
                    callback.onError("Communication with router failed.", e);
                } finally {
                    if (socket != null) {
                        socket.close();
                    }
                }
                return  null;
            }
        };
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
