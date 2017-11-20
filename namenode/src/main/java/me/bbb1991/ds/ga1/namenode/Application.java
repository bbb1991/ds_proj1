package me.bbb1991.ds.ga1.namenode;

import com.google.gson.Gson;
import me.bbb1991.ds.ga1.common.model.DataNode;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.ipc.RPC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
        openSocketToDataNode();
    }

    private static void openSocketToDataNode() {
        final ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(9001);
            new Thread(() -> {
                while (true) {
                    try (Socket socket = serverSocket.accept();
                         BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                        String line;
                        while (true) {
                            line = reader.readLine();
                            if (line == null) break;
                            LOGGER.info("Incoming message from datanode: {}", line);
                            Gson gson = new Gson();
                            dataNodes.add(gson.fromJson(line, DataNode.class));
                            LOGGER.info("Added new Datanode to list. Now list of datanode is: {}", dataNodes.size());
                        }
                        socket.close();
                    } catch (IOException e) {
                        LOGGER.error("ERROR!", e);
                        throw new RuntimeException(e);
                    }
                }

            }).start();
        } catch (IOException e) {
            LOGGER.error("ERROR!", e);
            e.printStackTrace();
        }
    }
}
