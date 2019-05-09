package com.keywestnetworks.kwconnect.ServerPrograms;

import com.hsq.kw.packet.vo.Configuration;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.util.Log;
import com.hsq.kw.packet.KeywestPacket;
import com.kw.connection.client.TCPClient;
import com.kw.connection.client.handler.AbstractResponseHandler;
import java.io.IOException;
import java.net.DatagramSocket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class RouterService {
    public static String TAG = RouterService.class.getName();
    public static final int DEFAULT_PORT = 9181;
    public static final long MAX_SUBSCRIPTION_AGE = TimeUnit.MINUTES.toMillis(60);
    public static final long REQUEST_TIMEOUT = TimeUnit.SECONDS.toMillis(10);

    public Map<Byte,Callback<KeywestPacket>> callbackMap = new HashMap<Byte,Callback<KeywestPacket>>();
    public int uniqueId = 0;
    private int connectionState = 0;
    private Byte previousByte = 0;
    private Byte connectionByte = new Byte((byte)0);
    private ResponseHandler handler = null;
    private boolean loginState = false;
    private Configuration oldConfiguration;
    private Configuration newConfiguration;

    private boolean enableSave = false;

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

    private static RouterService instance;

    private String mIPAdress;
    private int mPort;
    private boolean serverFound;
    private boolean isUserAuthenticated;
    private TCPClient client = null;
    private RouterService() {}

    public static RouterService getInstance() {
        synchronized(RouterService.class) {
            if (instance == null) {
                instance = new RouterService();
            }
        }
        return instance;
    }

    private class ResponseHandler extends AbstractResponseHandler {
        public ResponseHandler(TCPClient client) {
            super(client);
        }

        @Override
        public void responseReceived(byte[] bytes) {
            if (bytes != null) {
                try {
                    KeywestPacket returnPacket = new KeywestPacket(bytes);
                    byte[] bytes1 = returnPacket.getHeader().getHeader();
                    byte b = bytes1[7];
                    Log.d(RouterService.class.getName(),"Response received for sequence id" + b);
                    Callback<KeywestPacket> cb = callbackMap.get(b);
                    if (cb != null) {
                        Log.d(RouterService.class.getName(),"Calling call back" + cb);
                        cb.onSuccess(returnPacket);
                    }
                   // callbackMap.put(b,null);
                } catch (IOException e) {
                    //TODO Need to handle on error case from android side
                    //callbackMap.get(b).onError();
                }
            } else {
                Callback<KeywestPacket> cb = callbackMap.get(previousByte);
                callbackMap.put(previousByte,null);
                if (cb != null) {
                    cb.onError("No response received...",null);
                }
            }

        }

        @Override
        public void connectionFailed() {
            Callback<KeywestPacket> callback =  callbackMap.get(connectionByte);
            callbackMap.put(connectionByte,null);
            //callbackMap.remove(connectionByte);
            if (callback != null) {
                callback.onError("Unable to connect to server. Max retries reached",null);
            }
        }

        @Override
        public void serverConnected() {
            serverFound = true;
            Callback<KeywestPacket> callback =  callbackMap.get(connectionByte);
            callbackMap.put(connectionByte,null);
            if (callback != null) {
                callback.onSuccess(null);
            }
        }

        @Override
        public void serverDisconnected() {
            Byte by = previousByte;
            Log.i(RouterService.class.getName(), "Server disconnected called");
            Callback<KeywestPacket> callback =  callbackMap.get(by);
            connectionState = 0;
            if(callback != null)
                callback.onError("Server Disconnected",null);
            callbackMap.clear();
            serverFound = false;
            client.setCloseClient(true);
            setNewConfiguration(null);
            setOldConfiguration(null);
        }
    }
    public boolean isConnecting() {
        if (client != null && client.isConnected()) {
            return true;
        }
        return  false;
    }

    public void loginSuccess() {
        this.loginState = true;
    }

    public void loginFailed() {
        this.loginState = false;
    }

    public boolean isUserAuthenticated() {
        return loginState;
    }
    public int getConnectionState() {
        return connectionState;
    }

    public void setConnectionState(int connectionState) {
        this.connectionState = connectionState;
    }
    public boolean disconnect() {
        if (client != null) {
            Log.i(RouterService.class.getName(), "disconnect from wifi handler called");
            connectionState = 0;
           return client.disconnect();
        }
        return false;
    }
    public void connectTo(String ipAddress, Callback<KeywestPacket> callback) {
        Log.i(RouterService.class.getName(), "About to connect to server");
        client = new TCPClient(ipAddress,DEFAULT_PORT);
        client.setNoOfRetries(3);
        client.setTimeOutInSecs(2);
        connectionState = 1;
        handler = new ResponseHandler(client);
        client.initialize(handler);
        callbackMap.put(connectionByte,callback);
    }
    private synchronized byte getUniqueId() {
        if (uniqueId++ > 254) {
            uniqueId = 0;
        }
        return (byte)uniqueId;
    }

    public boolean isServerFound() {
        return serverFound;
    }

    public void authenticationFailed() {
        serverFound = false;
        loginFailed();
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
    }
    public void sendRequest(final KeywestPacket keywestPacket, final Callback<KeywestPacket> callback) {
        final byte b = getUniqueId();
        Thread th = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    keywestPacket.getHeader().setMore(b);
                    previousByte = b;
                    client.send(keywestPacket.toByteArray());
                    callbackMap.put(b,callback);
                } catch (IOException e) {
                    callback.onError(e.getMessage(), e);
                }
            }
        });
        th.start();
    }

    public void authRequest(final KeywestPacket keywestPacket, final Callback<KeywestPacket> callback) {
        final byte b = getUniqueId();
        Thread th = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    keywestPacket.getHeader().setMore(b);
                    previousByte = b;
                    client.send(keywestPacket.toByteArray());
                    callbackMap.put(b,callback);
                } catch (IOException e) {
                    callback.onError(e.getMessage(), e);
                }
            }
        });
        th.start();
    }

    public void sendRequest(final KeywestPacket keywestPacket, final Byte b,final Callback<KeywestPacket> callback) {
        Thread th = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    keywestPacket.getHeader().setMore(b);
                    client.send(keywestPacket.toByteArray());
                    callbackMap.put(b,callback);
                } catch (IOException e) {
                    callback.onError(e.getMessage(), e);
                }
            }
        });
        th.start();
    }
    private void sendRequest1(final KeywestPacket keywestPacket, final Callback<KeywestPacket> callback) {
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
    }

    public Subscription sendWithTimeOut(final KeywestPacket keywestPacket, final Callback<KeywestPacket> callback,long timeOutInterval) {
        final byte b = getUniqueId();
        Log.d(RouterService.class.getName(),"Sending timeout config with byte key + b");
        keywestPacket.getHeader().setMore(b);
        callbackMap.put(b,callback);
        sendRequest(keywestPacket,callback);
        final CountDownTimer timer = new CountDownTimer(timeOutInterval, 1000) {
            public void onTick(long millisUntilFinished){

            }
            public void onFinish() {
                //handler.responseReceived(null);
                Log.d(RouterService.class.getName(),"Sending timeout config with byte key + b");
                Callback<KeywestPacket> callback1 = callbackMap.get(b);
                if (callback1 != null) {
                    callback1.onError("Faild to receive message from device",null);
                    callbackMap.put(previousByte,null);
                }
                //callbackMap. callbackMap.put(connectionByte,null);
            }
        }.start();
        return new Subscription() {
            @Override
            public void cancel() {
                timer.cancel();
            }
        };
    }

    public Subscription observe(final KeywestPacket keywestPacket, final Callback<KeywestPacket> callback) {
        byte b = getUniqueId();
        Log.d(RouterService.class.getName(),"Sending observe summary config with byte key" + b);
        keywestPacket.getHeader().setMore(b);
        callbackMap.put(b,callback);
        final CountDownTimer timer = new CountDownTimer(MAX_SUBSCRIPTION_AGE, 1000) {
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

    public Configuration getOldConfiguration() {
        return oldConfiguration;
    }

    public Configuration getNewConfiguration() {
        return newConfiguration;
    }

    public void setOldConfiguration(Configuration oldConfiguration) {
        this.oldConfiguration = oldConfiguration;
    }

    public void setNewConfiguration(Configuration newConfiguration) {
        this.newConfiguration = newConfiguration;
    }

    public boolean isEnableSave() {
        return enableSave;
    }

    public void setEnableSave(boolean enableSave) {
        this.enableSave = enableSave;
    }

}