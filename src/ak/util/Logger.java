package ak.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Aleksander on 01:29, 08/07/2018.
 */
public class Logger {


    private static DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");


    public static void log(String m) {
        System.out.println(dateFormat.format(new Date()) + " : [message] : " + m);
    }

}
