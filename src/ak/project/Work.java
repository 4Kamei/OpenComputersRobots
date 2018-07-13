package ak.project;

import ak.entity.Robot;

/**
 * Created by Aleksander on 00:57, 11/07/2018.
 */
public interface Work {
    void assign(Robot r);

    void start();

    Robot getWorker();

}
