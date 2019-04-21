package com.keywestnetworks.kwconnect.Interfaces;

import com.hsq.kw.packet.KeywestPacket;

public interface TaskCompleted {

    public void onTaskComplete(KeywestPacket result);
    public void endServer(String result);

    public void responce(KeywestPacket responce);
}
