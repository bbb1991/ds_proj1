package me.bbb1991.ds.ga1.namenode.service;

import me.bbb1991.ds.ga1.common.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
public class NameNodeService {

    private List<Chunk> testChunk; // TODO remove mess

    public NameNodeService() {
        dataNodes = new ArrayList<>();


        testChunk = new ArrayList<>();
        testChunk.add(new Chunk("file1.txt", "20170101082350", 1, "localhost", 9001, 100, FileType.FILE, 0));
        testChunk.add(new Chunk("file2.txt", "20171101082350", 1, "localhost", 9001, 100, FileType.FILE, 0));
        testChunk.add(new Chunk("file3.txt", "20171101082350", 1, "localhost", 9001, 100, FileType.FILE, 0));
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(NameNodeService.class);

    private DBService dbService; // TODO add logic with DS service

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
                                Optional<Chunk> optional = testChunk.stream().filter(e -> e.getOriginalName().equalsIgnoreCase(fileName)).findFirst();
                                LOGGER.info("Is file found: {}", optional.isPresent());
                                if (!optional.isPresent()) {
                                    out.writeObject(new Container<>(CommandType.ERROR));
                                    return;
                                }

                                Chunk chunk = optional.get();
                                LOGGER.info("Getting file: {}", chunk);
                                out.writeObject(new Container<List<Chunk>>(CommandType.OK, new ArrayList<>(Collections.singleton(chunk)), null));
                                break;

                            case MKDIR:
                                String folderName = (String) container.getObject();
                                LOGGER.info("Creating folder with name: {}", folderName);

                                dbService.saveObject(new Chunk(folderName, 0, null, 0, 0, FileType.FOLDER, 0));
                                out.writeObject(new Container<Void>(CommandType.OK));
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
