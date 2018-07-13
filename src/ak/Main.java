package ak;

import ak.external.HttpFileContentHandler;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Created by Aleksander on 22:10, 06/07/2018.
 */
public class Main {


    private HttpServer s;
    public static void main(String[] args) throws IOException {

        Main m = new Main();

        ConnectionManager robotServer = new ConnectionManager();

    }

    public Main() throws IOException {
        s = HttpServer.create(new InetSocketAddress(80), 0);

        addAll(new File("res"));

        s.setExecutor(null);

        System.out.println(s.getAddress());

        s.start();


    }

    private void addAll(File f) {
        for (File file : f.listFiles()) {
            System.out.println("Looping for " + file);
            if (file.isDirectory()) {
                System.out.println("Is Dir " + file);
                addAll(file);
            } else if (file.isFile() ) {
                System.out.println("Is file " + file);
                try {
                    System.out.println("Path " + file.getPath().replace("\\", "/").replace("res/", ""));
                    s.createContext("/" + file.getPath().replace("\\", "/").replace("res/", ""), new HttpFileContentHandler(file));
                } catch (Exception e) {
                    e.printStackTrace();
                    System.err.println(e.getMessage() + "  Error for file " + file);
                }
            }
        }
    }


}
