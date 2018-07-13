package ak;

import ak.OperationManager;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Created by Aleksander on 23:59, 08/07/2018.
 */
public class ConnectionManager {

    public ConnectionManager() throws IOException {


        ServerSocket s = new ServerSocket(12345);

        OperationManager m = new OperationManager();
        Thread serverThread = new Thread(m);

        serverThread.start();

        while (true) {
             m.addPreInit(s.accept());
        }

    }
}
