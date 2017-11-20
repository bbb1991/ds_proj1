package me.bbb1991.ds.ga1.datanode;

import me.bbb1991.ds.ga1.common.Utils;
import me.bbb1991.ds.ga1.common.model.DataNode;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.ipc.RPC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

@SpringBootApplication
public class Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) throws Exception {

        SpringApplication.run(Application.class, args);

        int port = Utils.getRandomPort();

        Configuration conf = new Configuration();

        RPC.Server server = new RPC.Builder(conf)
                .setProtocol(DataNodeProtocol.class)
                .setInstance(new DataNodeProtocolImpl())
                .setBindAddress("0.0.0.0")
                .setNumHandlers(2)
                .setPort(port)
                .setVerbose(true)
                .build();
        server.start();

        notifyNameNode();

        server.stop();
    }

    /**
     * Method that sends info message to namenode. Info message says that new datanode came up and ready to receive
     * commands. Also info message contains useful information, such as ip address/host and which port opened to
     * receive commands
     */
    private static void notifyNameNode() {
        LOGGER.info("Sending hello message to namenode");
        try (Socket socket = new Socket("localhost", 9002);
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream())) {
            objectOutputStream.writeObject(new DataNode("localhost", 9090));
        } catch (IOException e) {
            LOGGER.error("Error while sending hello message to namenode!", e);
            throw new RuntimeException(e);
        }
    }
}
