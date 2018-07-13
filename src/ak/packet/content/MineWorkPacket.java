package ak.packet.content;

import ak.project.ProjectType;

/**
 * Created by Aleksander on 20:08, 09/07/2018.
 */
public class MineWorkPacket {

    private int workID;
    private int x;
    private int y;
    private int z;
    private int sizeX;
    private int sizeY;
    private int sizeZ;
    private int projID;
    private final ProjectType type = ProjectType.MINE_PROJECT;

    public MineWorkPacket(int projID, int workID, int x, int y, int z, int sizeX, int sizeY, int sizeZ) {
        this.projID = projID;
        this.workID = workID;
        this.x = x;
        this.y = y;
        this.z = z;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
    }

}
