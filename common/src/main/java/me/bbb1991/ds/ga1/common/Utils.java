package me.bbb1991.ds.ga1.common;

import java.io.IOException;
import java.net.ServerSocket;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Class that contains various useful methods.
 *
 * @author Bagdat Bimaganbetov
 * @author b.bimaganbetov@innopolis.ru
 */
public class Utils {

    /**
     * Method that gets random port from machine.
     *
     * @return random port that can be opened to receive messages/commands
     * @throws IOException rethrow basic error
     */
    public static int getRandomPort() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            return serverSocket.getLocalPort();
        }
    }

    /**
     * Returns file name from current timestamp. Example: <code>2017110024174717</code>
     *
     * @return timestamp in string
     */
    public static String getFileName() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddddHHmmss");
        return simpleDateFormat.format(new Date());
    }
}
