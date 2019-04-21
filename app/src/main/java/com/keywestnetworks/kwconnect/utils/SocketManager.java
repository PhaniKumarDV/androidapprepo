package com.keywestnetworks.kwconnect.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.keywestnetworks.kwconnect.Interfaces.TaskCompleted;
import com.hsq.kw.packet.KeywestPacket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class SocketManager {


    private static SocketManager instance;

    private SocketManager() {
    }

    /**
     * Gets instance.
     *
     * @return the instance
     */
    public synchronized static SocketManager getInstance() {
        if (instance == null) {
            instance = new SocketManager();
        }
        return instance;
    }

    /**
     * The constant TAG.
     */

    private Socket socket;


    public void connectSocket() {
        try {
            if(socket==null)
            {
                Log.i("connect","connected");
                ConnectToServer connectToServer = new ConnectToServer();
                connectToServer.execute();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class ConnectToServer extends AsyncTask<String, String, String> {

        protected String doInBackground(String... urls) {
            try {

                socket = new Socket("192.168.2.217",9876);

                return null;
            } catch (Exception e) {
                // this.exception = e;
                return null;
            }
        }
    }

    public void Request(Context context,KeywestPacket testPacket) {

        try {
            if(socket!=null)
            {
                 Request request = new Request(context,testPacket);
                 request.execute();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public class Request extends AsyncTask<KeywestPacket, KeywestPacket, KeywestPacket> {

        private Context mContext;
        private KeywestPacket mKeywestPacket;
        private TaskCompleted mCallback;

        public Request(Context context,KeywestPacket testPacket){
            this.mContext = context;
            this.mCallback = (TaskCompleted) context;
            this.mKeywestPacket = testPacket;

        }
        protected final KeywestPacket doInBackground(KeywestPacket... testPackets) {
            try {
                byte[] buffer = new byte[1307];
                DataInputStream dis = new DataInputStream(socket.getInputStream());
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                byte[] bytes = mKeywestPacket.toByteArray();
                dos.write(bytes);
                dis.read(buffer);
                KeywestPacket recivedPacket = new KeywestPacket(buffer);
                Log.w("recivedPacket", String.valueOf(recivedPacket));

                return recivedPacket;
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute (KeywestPacket results){
            mCallback.onTaskComplete(results);
        }
    }

}

