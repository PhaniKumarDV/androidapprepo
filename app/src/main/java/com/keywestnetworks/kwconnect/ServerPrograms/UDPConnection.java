package com.keywestnetworks.kwconnect.ServerPrograms;

import android.content.Context;

import com.keywestnetworks.kwconnect.Interfaces.TaskCompleted;
import com.hsq.kw.packet.KeywestPacket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/***
 * @deprecated
 * Use {@link RouterService}
 */
@Deprecated()
public class UDPConnection extends Thread{

    private String dstAddress;
    private int dstPort;
    private boolean running;
    private TaskCompleted mCallback;
    private KeywestPacket keywestPacket;
    Context mContext;
    private DatagramSocket socket;

    public UDPConnection(String addr, int port,KeywestPacket RequestPacket,Context context) {
        super();
        dstAddress     = addr;
        dstPort        = port;
        this.mCallback = (TaskCompleted) context;
        keywestPacket  = RequestPacket;
    }

    public UDPConnection(String addr, int port,KeywestPacket RequestPacket,TaskCompleted listener) {
        super();
        dstAddress     = addr;
        dstPort        = port;
        this.mCallback = (TaskCompleted) listener;
        keywestPacket  = RequestPacket;
    }


    public void setRunning(boolean running){
        this.running = running;
    }

    @Override
    public void run() {
        running = true;

        try {
            socket = new DatagramSocket();
            InetAddress address = InetAddress.getByName(dstAddress);
            byte[] bytes =  keywestPacket.toByteArray();
            DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, dstPort);
            socket.send(packet);

            byte[] buf = new byte[1307];
            packet = new DatagramPacket(buf, buf.length);
            socket.receive(packet);

            KeywestPacket recivedPacket = new KeywestPacket(packet);
            mCallback.onTaskComplete(recivedPacket);

        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(socket != null){
                socket.close();
                mCallback.endServer("Server Closed");
            }
        }
    }

}
