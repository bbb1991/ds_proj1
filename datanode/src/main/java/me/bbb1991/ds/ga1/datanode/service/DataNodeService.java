package me.bbb1991.ds.ga1.datanode.service;

import me.bbb1991.ds.ga1.common.Utils;
import me.bbb1991.ds.ga1.common.model.CommandType;
import me.bbb1991.ds.ga1.common.model.DataNode;
import me.bbb1991.ds.ga1.common.model.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Controller;

import javax.annotation.PostConstruct;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Class with various methods that works with remote servers.
 */
@Controller
@PropertySource("classpath:application.properties")
public class DataNodeService {

    @Value("${datanode.working_dir}")
    private String workingPath;

    @Value("${namenode.port}")
    private int namenodePort;

    @Value("${namenode.host}")
    private String namenodeHost;

    private String datanodeHost;

    private int datanodePort;

    private static final Logger LOGGER = LoggerFactory.getLogger(DataNode.class);

    /**
     * Method that sends info message to name node. Info message says that new datanode came up and ready to receive
     * commands. Also info message contains useful information, such as ip address/host and which port opened to
     * receive commands
     */
    public void notifyNameNode() {
        LOGGER.info("Sending hello message to namenode");
        try (Socket socket = new Socket(namenodeHost, namenodePort);
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {
            Thread.sleep(1000);
            out.writeObject(CommandType.HELLO);
            out.writeObject(new DataNode(datanodeHost, datanodePort));
        } catch (Exception e) {
            LOGGER.error("Error while sending hello message to namenode!", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Opening socket and listen port
     */
    public void openSocket() {
        final ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(datanodePort);
            new Thread(() -> {
                while (true) {
                    try (Socket socket = serverSocket.accept();
                         ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                         ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                         DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                    ) {
                        CommandType command = (CommandType) in.readObject();
                        LOGGER.info("Got command: {}", command);
                        switch (command) {
                            case UPLOAD_FILE:
                                byte[] buffer = new byte[4096];
                                long size = (long) in.readObject();
                                String filename = (String) in.readObject();
                                LOGGER.info("Saving file as: {}", workingPath + File.separator + filename);
                                try (FileOutputStream fileOutputStream = new FileOutputStream(workingPath + File.separator + filename)) {
                                    int read;
                                    long totalRead = 0;
                                    long remaining = size;
                                    while ((read = dataInputStream.read(buffer, 0, (int) Math.min(buffer.length, remaining))) > 0) {
                                        totalRead += read;
                                        remaining -= read;
                                        LOGGER.info("Read {} bytes", totalRead);
                                        fileOutputStream.write(buffer, 0, read);
                                    }
                                    LOGGER.info("File saved!"); // todo unlock file in namenode
                                }
                                out.writeObject(Status.OK);
                                break;

                            case GET:
                                filename = (String) in.readObject();
                                for (File file : new File(workingPath).listFiles()) {
                                    LOGGER.info("Comparing file {} and {}", filename, file.getName());
                                    if (filename.equalsIgnoreCase(file.getName())) {
                                        LOGGER.info("Matched! Sending file to client");
                                        out.writeObject(Status.OK);
                                        try (FileInputStream fileInputStream = new FileInputStream(file);
                                             DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                                        ) {
                                            buffer = new byte[4096];

                                            while (fileInputStream.read(buffer) > 0) {
                                                dataOutputStream.write(buffer);
                                            }
                                            dataOutputStream.flush();
                                        }
                                        return;
                                    }
                                }

                                out.writeObject(Status.FILE_NOT_FOUND);
                                break;

                            case HEARTBEAT:
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

    @PostConstruct
    public void init() throws IOException {
        datanodePort = System.getProperty("datanode.port", null) == null ? Utils.getRandomPort() : Integer.parseInt(System.getProperty("datanode.port"));
        datanodeHost = System.getProperty("datanode.host", null) == null ? "0.0.0.0" : System.getProperty("datanode.host");
    }
}
