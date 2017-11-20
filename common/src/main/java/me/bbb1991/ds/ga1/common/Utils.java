package me.bbb1991.ds.ga1.common;

import java.io.IOException;
import java.net.ServerSocket;

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
}
