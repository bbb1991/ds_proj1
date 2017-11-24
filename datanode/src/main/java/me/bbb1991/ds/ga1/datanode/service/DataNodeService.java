package me.bbb1991.ds.ga1.datanode.service;

import me.bbb1991.ds.ga1.common.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

@Controller
public class DataNodeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataNode.class);

    /**
     * Method that sends info message to name node. Info message says that new datanode came up and ready to receive
     * commands. Also info message contains useful information, such as ip address/host and which port opened to
     * receive commands
     */
    public void notifyNameNode() {
        LOGGER.info("Sending hello message to namenode");
        try (Socket socket = new Socket("localhost", 9002);
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream())) {
            objectOutputStream.writeObject(new DataNode("localhost", 9090));
        } catch (IOException e) {
            LOGGER.error("Error while sending hello message to namenode!", e);
            throw new RuntimeException(e);
        }
    }

    public void openSocket() {
        final ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(9090);
            new Thread(() -> {
                while (true) {
                    try (Socket socket = serverSocket.accept();
                         ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                         ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                    ) {
                        CommandType command = (CommandType) in.readObject();
                        LOGGER.info("Got container type: {}", command);
                        switch (command) {
                            case UPLOAD_FILE:

                                File file = (File) in.readObject();
                                String fileName = (String) in.readObject();
                                LOGGER.info("Saving new file: {}, with name: {}", file, fileName);
                                try (FileOutputStream fileOutputStream = new FileOutputStream(fileName);
                                     ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream)) {
                                    objectOutputStream.writeObject(file);
                                }
                                LOGGER.info("File saved!");
                                out.writeObject(Status.OK);

                                break;

                            default:
                                throw new RuntimeException("Not implemented!");

                        }
                    } catch (Exception e) {
                        LOGGER.error("ERROR!", e);
                        throw new RuntimeException(e);
                    }
                }

            }).start();
        } catch (IOException e) {
            LOGGER.error("ERROR!", e);
        }
    }
}
