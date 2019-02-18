package com.hitsquadtechnologies.sifyconnect.ServerPrograms;

import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.util.Log;

import com.hsq.kw.packet.KeywestPacket;
import com.kw.connection.client.TCPClient;
import com.kw.connection.client.handler.AbstractResponseHandler;

import java.io.IOException;
import java.net.DatagramSocket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class RouterService {

    public static String TAG = RouterService.class.getName();

    public static final int DEFAULT_PORT = 9181;
    public static final long MAX_SUBSCRIPTION_AGE = TimeUnit.MINUTES.toMillis(60);
    public static final long REQUEST_TIMEOUT = TimeUnit.SECONDS.toMillis(10);

    public Map<Byte,Callback<KeywestPacket>> callbackMap = new ConcurrentHashMap<Byte,Callback<KeywestPacket>>();

    public int uniqueId = 0;

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

    private TCPClient client = null;

    private RouterService() {}

    private class ResponseHandler extends AbstractResponseHandler {

        public ResponseHandler(TCPClient client) {
            super(client);
        }

        @Override
        public void responseReceived(byte[] bytes) {
            try {
                KeywestPacket returnPacket = new KeywestPacket(bytes);
                byte[] bytes1 = returnPacket.getHeader().getHeader();
                byte b = bytes1[7];
                Callback<KeywestPacket> cb = callbackMap.get(b);
                if (cb != null) {
                    cb.onSuccess(returnPacket);
                }

            } catch (IOException e) {
                //TODO Need to handle on error case from android side
                //callbackMap.get(b).onError();
            }

        }

        @Override
        public void serverConnected() {
            Byte by = new Byte((byte)0);
            serverFound = true;
            Callback<KeywestPacket> callback =  callbackMap.get(by);
            callback.onSuccess(null);
        }

        @Override
        public void serverDisconnected() {
            callbackMap.clear();
            serverFound = false;
        }
    }

    public boolean isConnecting() {
        if (client != null && client.isConnected()) {
            return true;
        }
        return  false;
    }


    public void connectTo(String ipAddress, Callback<KeywestPacket> callback) {
        client = new TCPClient(ipAddress,DEFAULT_PORT);
        ResponseHandler handler = new ResponseHandler(client);
        client.initialize(handler);
        callbackMap.put((byte)0,callback);
        //connectTo(ipAddress, DEFAULT_PORT, callback);
    }

    private synchronized byte getUniqueId() {
        if (uniqueId++ > 254) {
            uniqueId = 0;
        }
        return (byte)uniqueId;
    }

    public void connectTo(String ipAddress, int port, final Callback<KeywestPacket> callback) {
        this.mIPAdress = ipAddress;
        this.mPort = port;
        KeywestPacket wirelessLinkPacket = new KeywestPacket((byte)1, (byte)1, (byte)2);
        wirelessLinkPacket.getHeader().setMore(getUniqueId());
        //wirelessLinkPacket.getHeader().s
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
                byte b = getUniqueId();
                keywestPacket.getHeader().setMore(b);
                callbackMap.put(b,callback);

               /* try {
                    mSocket = new DatagramSocket()
                } catch (Exception e) {
                    throw new Error("Could not initiate data gram connection");
                }*/
            }

            @Override
            protected void onPostExecute(Object o) {
                /*super.onPostExecute(o);
                if (mSocket != null) {
                    mSocket.close();
                }*/
            }

            @Override
            protected KeywestPacket doInBackground(Object... objects) {
                //byte b = getUniqueId();
                //keywestPacket.getHeader().setMore(b);
                //callbackMap.put(b,callback);
                try {
                    client.send(keywestPacket.toByteArray());
                } catch (IOException e) {

                }
               return  keywestPacket;
            }
        };
        return task;
    }


    public void sendReq(final KeywestPacket keywestPacket, final Callback<KeywestPacket> callback) {
        byte b = getUniqueId();
        keywestPacket.getHeader().setMore(b);
        callbackMap.put(b,callback);
        try {
            client.send(keywestPacket.toByteArray());
        } catch (IOException e) {

        }
        //send(keywestPacket, callback).execute();
    }

    public void sendRequest(final KeywestPacket keywestPacket, final Callback<KeywestPacket> callback) {
        byte b = getUniqueId();
        keywestPacket.getHeader().setMore(b);
        callbackMap.put(b,callback);
        Thread th = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    client.send(keywestPacket.toByteArray());
                } catch (IOException e) {

                }
            }
        });
        th.start();
        //send(keywestPacket, callback).execute();
    }

    public void sendRequest1(final KeywestPacket keywestPacket, final Callback<KeywestPacket> callback) {

        Thread th = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    client.send(keywestPacket.toByteArray());
                } catch (IOException e) {

                }
            }
        });
        th.start();
        //send(keywestPacket, callback).execute();
    }

    public Subscription observe(final KeywestPacket keywestPacket, final Callback<KeywestPacket> callback) {
        byte b = getUniqueId();
        keywestPacket.getHeader().setMore(b);
        callbackMap.put(b,callback);
        final CountDownTimer timer = new CountDownTimer(MAX_SUBSCRIPTION_AGE, 3000) {
            public void onTick(long millisUntilFinished){
                sendRequest1(keywestPacket, callback);
            }
            public void onFinish() {
                sendRequest1(keywestPacket, callback);
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
