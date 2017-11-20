package me.bbb1991.ds.ga1.namenode;

import com.google.gson.Gson;
import me.bbb1991.ds.ga1.common.model.Command;
import me.bbb1991.ds.ga1.common.model.DataNode;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.ipc.RPC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    private static List<DataNode> dataNodes;

    public static void main(String[] args) throws Exception {

        SpringApplication.run(Application.class, args);

        dataNodes = new ArrayList<>();

        Configuration conf = new Configuration();

        RPC.Server server = new RPC.Builder(conf)
                .setProtocol(NameNodeProtocol.class)
                .setInstance(new NameNodeProtocolImpl())
                .setBindAddress("0.0.0.0")
                .setNumHandlers(2)
                .setVerbose(true)
                .setPort(9000)
                .build();
        server.start();
        openSocketToClient();
        openSocketToDataNode();
    }

    private static void openSocketToDataNode() {
        final ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(9002);
            new Thread(() -> {
                while (true) {
                    try (Socket socket = serverSocket.accept();
                         ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream())
                    ) {
                        DataNode dataNode = (DataNode) objectInputStream.readObject();
                        dataNodes.add(dataNode);
                        LOGGER.info("{}", dataNode);
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

    private static void openSocketToClient() {
        final ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(9001);
            new Thread(() -> {
                while (true) {
                    try (Socket socket = serverSocket.accept();
                         ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                         ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
                    ) {
                        Command command = (Command) objectInputStream.readObject();
                        LOGGER.info("Got command type: {}", command.getCommandType());
                        objectOutputStream.writeObject("OK");
                        socket.close();
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
