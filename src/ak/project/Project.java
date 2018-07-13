package ak.project;

import ak.entity.Robot;

/**
 * Created by Aleksander on 21:49, 09/07/2018.
 */
public interface Project {


    public boolean start();

    public boolean isStarted();

    public boolean isEnded();

    public boolean isStopped();

    public ProjectState getState();

    public void addWorker(Robot r);

    public void update();

    public void stop();

    int getID();
}
