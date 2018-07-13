package ak.external;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.util.stream.Collectors;

/**
 * Created by Aleksander on 19:31, 07/07/2018.
 */
public class HttpFileContentHandler implements HttpHandler {

    private byte[] fileContent;
    private String name;

    public HttpFileContentHandler(File f) throws IOException {
        this.name = f.getPath();
        System.out.println("Adding handler for file " + f.getPath());

        BufferedReader r = new BufferedReader(new FileReader(f));

        char[] chars = r.lines().collect(Collectors.joining("\n")).toCharArray();

        fileContent = new byte[chars.length];
        for (int i = 0; i < fileContent.length; i++) {
            fileContent[i] = (byte) chars[i];
        }

        r.close();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
            System.out.println("Handling " + name);
            exchange.sendResponseHeaders(200, fileContent.length);

            OutputStream os = exchange.getResponseBody();
            os.write(fileContent);
            os.close();
    }


}
