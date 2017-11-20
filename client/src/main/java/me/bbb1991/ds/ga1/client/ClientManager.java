package me.bbb1991.ds.ga1.client;

import me.bbb1991.ds.ga1.common.model.Command;
import me.bbb1991.ds.ga1.common.model.CommandType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

@Component
public class ClientManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientManager.class);

    public void getListOfMessages() {
        LOGGER.info("Sending new command to namenode");
        try (Socket socket = new Socket("localhost", 9001);
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
        ) {
            objectOutputStream.writeObject(new Command<Void>(CommandType.LIST_FILES));
            Object o = objectInputStream.readObject();
            LOGGER.info("Get answer: {}", o);
        } catch (Exception e) {
            LOGGER.error("ERROR!", e);
        }
    }
}
