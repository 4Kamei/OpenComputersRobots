package ak.project;

import ak.util.Logger;
import ak.entity.Robot;
import ak.packet.Packet;
import ak.packet.PacketType;
import ak.packet.content.MineWorkPacket;

/**
 * Created by Aleksander on 22:07, 09/07/2018.
 */
public class MineWork implements Work {

    private long timeCreated;
    private int x, y, z;
    private int workID;
    private int projID;
    private int sizeX, sizeY, sizeZ;
    private Robot worker;
    private boolean isFinished;
    private boolean isStarted;

    public MineWork(int projID, int workID, int x, int y, int z, int sizeX, int sizeY, int sizeZ) {
        this.timeCreated = System.currentTimeMillis();
        this.workID = workID;
        this.projID = projID;
        this.x = x;
        this.y = y;
        this.z = z;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
    }


    @Override
    public void assign(Robot r) {
        if (worker == null) {
            this.worker = r;
        } else {
            Logger.log("Could not assign robot " + r.getAddress() + " to work as already assigned");
        }
    }

    @Override
    public void start() {
        if (isStarted) {
            Logger.log("Work already started from proj = " + projID + " work = " + workID);
        }

        isStarted = true;
        MineWorkPacket cont = new MineWorkPacket(projID, workID, x, y, z, sizeX, sizeY, sizeZ);
        Packet p = new Packet(cont, null, PacketType.C_WORK_PACKET);
        worker.sendPacket(p);
    }

    @Override
    public Robot getWorker() {
        return worker;
    }

    public Integer getID() {
        return workID;
    }
}
