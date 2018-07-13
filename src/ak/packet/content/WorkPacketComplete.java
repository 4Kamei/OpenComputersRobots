package ak.packet.content;

/**
 * Created by Aleksander on 02:21, 11/07/2018.
 */
public class WorkPacketComplete {

    private int workID;
    private int projID;

    public int getWorkID() {
        return workID;
    }

    public int getProjID() {
        return projID;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("WorkPacketComplete{");
        sb.append("workID=").append(workID);
        sb.append(", projID=").append(projID);
        sb.append('}');
        return sb.toString();
    }

    //Some extra fluff maybe?

}
