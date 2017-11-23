package me.bbb1991.ds.ga1.namenode.service;

import me.bbb1991.ds.ga1.common.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

@Component
public class NameNodeService {

    public NameNodeService() {
        dataNodes = new ArrayList<>();
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(NameNodeService.class);

    private DBService dbService;

    private List<DataNode> dataNodes;


    public void openSocketToDataNode() {
        final ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(9002);
            new Thread(() -> {
                while (true) {
                    try (Socket socket = serverSocket.accept();
                         ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream())
                    ) {
                        DataNode dataNode = (DataNode) objectInputStream.readObject();
                        LOGGER.info("New Data Node came up!: {}", dataNode);
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

    public void openSocketToClient() {
        final ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(9001);
            new Thread(() -> {
                while (true) {
                    try (Socket socket = serverSocket.accept();
                         ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                         ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                    ) {
                        Container container = (Container) in.readObject();
                        LOGGER.info("Got container type: {}", container.getCommandType());
                        switch (container.getCommandType()) {
                            case LIST_FILES:
                                List<Chunk> files = dbService.getAllFiles("/"); // TODO various folder logic
                                LOGGER.info("Files: {}", files.size());
                                out.writeObject(new Container<>(CommandType.OK, files, null));
                                break;

                            case GET:
                                String fileName = (String) container.getObject();
//                                List<Chunk> f  = dbService.getFilesByName(fileName);
                                LOGGER.info("Getting filename: {}", fileName);
                                break;


                            case MKDIR:
                                String folderName = (String) container.getObject();
                                LOGGER.info("Creating folder with name: {}", folderName);

                                dbService.saveObject(Chunk.builder().setOriginalName(folderName).setDatatype(FileType.FOLDER).build());
                                out.writeObject(new Container<Void>(CommandType.OK));
                                break;

                            case UPLOAD_FILE:
                                File file = (File) container.getObject();
                                LOGGER.info("Saving file: {}", file);
                                try (FileOutputStream fileOutputStream = new FileOutputStream(file.getName());
                                     ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream)) {
                                    objectOutputStream.writeObject(file);
                                }
                                LOGGER.info("File saved!");
                                out.writeObject(new Container<Void>(CommandType.OK));
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

    @Autowired
    public void setDbService(DBService dbService) {
        this.dbService = dbService;
    }

    @Autowired
    public void setDataNodes(List<DataNode> dataNodes) {
        this.dataNodes = dataNodes;
    }
}
