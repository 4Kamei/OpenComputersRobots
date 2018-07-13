package ak.project;

import ak.entity.Robot;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Aleksander on 20:22, 09/07/2018.
 */

//Project represents the biggest possible single thing a group of robots can be working on
//Each robot is assigned "Work" from the project
public class MineProject implements Project {

    //Job Position
    private int x;
    private int y;
    private int z;
    private int sizeX;
    private int sizeY;
    private int sizeZ;

    private ArrayList<Robot> unassignedWorkers;
    private ProjectState state;
    private ArrayList<MineWork> unassignedWork;
    private HashMap<Integer, MineWork> assignedWork;
    private ArrayList<MineWork> completeWork;
    private final int chunk_size = 5;
    private int maxID = 0;
    private int projID;

    public MineProject(int projID, int x0, int y0, int z0, int sizeX, int sizeY, int sizeZ) {
        this.projID = projID;
        this.x = x0;
        this.y = y0;
        this.z = z0;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;

        unassignedWorkers = new ArrayList<>();

        unassignedWork = new ArrayList<>();
        assignedWork = new HashMap<>();
        completeWork = new ArrayList<>();

        state = ProjectState.SCHEDULED;
        segmentIntoWork();

    }

    private void segmentIntoWork() {
        int xDivs = sizeX / chunk_size;
        int xLeftover = sizeX % chunk_size;

        int zDivs = sizeZ / chunk_size;
        int zLeftover = sizeZ % chunk_size;

        for (int curDivX = 0; curDivX < xDivs; curDivX++) {
            for (int curDivZ = 0; curDivZ < zDivs; curDivZ++) {
                unassignedWork.add(new MineWork(projID, newID(), x + curDivX  * chunk_size, y, z + curDivZ * chunk_size, chunk_size, sizeY, chunk_size));
            }
            if (zLeftover > 0)
                unassignedWork.add(new MineWork(projID, newID(), x + curDivX * chunk_size, y, z + zDivs * chunk_size, chunk_size, sizeY, zLeftover));
        }
        if (xLeftover > 0) {
            for (int curDivZ = 0; curDivZ < zDivs; curDivZ++) {
                unassignedWork.add(new MineWork(projID, newID(), x + xDivs  * chunk_size, y, z + curDivZ * chunk_size, xLeftover, sizeY, chunk_size));
            }
            if (zLeftover > 0) {
                unassignedWork.add(new MineWork(projID, newID(), x + xDivs * chunk_size, y, z + zDivs * chunk_size, xLeftover, sizeY, zLeftover));
            }
        }
    }


    @Override
    public boolean start() {
        if (unassignedWorkers.size() == 0) {
            return false;
        }
        this.state = ProjectState.RUNNING;
        return true;
    }

    @Override
    public boolean isStarted() {
        return state == ProjectState.RUNNING;
    }

    @Override
    public boolean isEnded() {
        return state == ProjectState.STOPPED_ERROR || state == ProjectState.COMPLETE;
    }

    public boolean isStopped() {
        return state == ProjectState.STOPPED;
    }

    @Override
    public ProjectState getState() {
        return state;
    }

    public void workFinished(int workID) {
        MineWork w = assignedWork.get(workID);
        assignedWork.remove(workID);
        completeWork.add(w);
        unassignedWorkers.add(w.getWorker());
        w.getWorker().free();

        if (state == ProjectState.STOPPING && assignedWork.size() == 0) {
            state = ProjectState.STOPPED;

            //TODO UNALLOCATE WORKERS
        }
    }

    @Override
    public void update() {
        if (!isStarted())
            return;
        if (state != ProjectState.RUNNING)
            return;
        if (assignedWork.size() > 0 || unassignedWork.size() > 0) {
            while (unassignedWorkers.size() > 0 && unassignedWork.size() > 0) {
                MineWork w = unassignedWork.get(0);
                unassignedWork.remove(0);
                Robot r = unassignedWorkers.get(0);
                unassignedWorkers.remove(0);
                w.assign(r);
                assignedWork.put(w.getID(), w);
                w.start();
                r.startWork();
            }
        }
        if (assignedWork.size() == 0 && unassignedWork.size() == 0) {
            state = ProjectState.COMPLETE;
        }
    }

    @Override
    public void stop() {
        this.state = ProjectState.STOPPING;
    }

    private Integer newID() {
        return maxID++;
    }

    @Override
    public void addWorker(Robot r) {
        unassignedWorkers.add(r);
    }

    @Override
    public String toString() {

        return String.format("MineProject{ state=%s pos=(%d, %d, %d) size=(%d, %d, %d) work=(%d, %d, %d) workers=(%d)", state, x, y, z, sizeX, sizeY, sizeZ, unassignedWork.size(), assignedWork.size(), completeWork.size(), unassignedWorkers.size());
    }

    public int getID() {
        return projID;
    }
}
