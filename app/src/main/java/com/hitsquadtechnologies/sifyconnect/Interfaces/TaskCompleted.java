package com.hitsquadtechnologies.sifyconnect.Interfaces;

import com.hsq.kw.packet.KeywestPacket;


import java.util.List;

public interface TaskCompleted {

    public void onTaskComplete(KeywestPacket result);
    public void endServer(String result);

    public void responce(KeywestPacket responce);
}
