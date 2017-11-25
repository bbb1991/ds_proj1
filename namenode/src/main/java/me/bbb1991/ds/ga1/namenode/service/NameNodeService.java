package me.bbb1991.ds.ga1.namenode.service;

import me.bbb1991.ds.ga1.common.Utils;
import me.bbb1991.ds.ga1.common.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
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
                        CommandType command = (CommandType) in.readObject();
                        LOGGER.info("Got command from client: {}", command);
                        switch (command) {
                            case LIST_FILES:
                                LOGGER.info("Getting path info");
                                String path = (String) in.readObject();
                                LOGGER.info("Got path info. Forming answer");
                                List<Chunk> files = dbService.getAllFiles(path); // TODO various folder logic
                                LOGGER.info("Files: {}", files.size());
                                out.writeObject(Status.OK);
                                out.writeObject(files);
                                break;

                            case GET:
                                String fileName = (String) in.readObject(); // TODO
                                List<Chunk> f  = dbService.getFilesByName(fileName);
                                LOGGER.info("Getting filename: {}", fileName);
                                out.writeObject(Status.OK);
                                out.writeObject(f);
                                break;


                            case MKDIR:
                                String folderName = (String) in.readObject();
                                LOGGER.info("Creating folder with name: {}", folderName);

                                dbService.saveObject(Chunk.builder().setOriginalName(folderName).setDatatype(FileType.FOLDER).build());
                                out.writeObject(Status.OK);
                                break;

                            case UPLOAD_FILE:
                                long fileSize = (long) in.readObject(); // todo do something with file size
                                String originalName = (String) in.readObject(); // todo do something with file size

                                LOGGER.info("Original name is: {}", originalName);
                                if (dataNodes.isEmpty()) {
                                    LOGGER.warn("No data node available to save file!");
                                    out.writeObject(Status.NO_DATANODE_AVAILABLE);
                                    break;
                                }

                                DataNode dataNode = dataNodes.get(0);

                                String filename = Utils.getFileName();
                                LOGGER.info("File name is: {}", filename);

                                Chunk chunk = Chunk.builder()
                                        .setDatatype(FileType.FILE)
                                        .setOriginalName(originalName)
                                        .setFileName(filename)
                                        .setLocked(true)
                                        .setFileSize(fileSize)
                                        .setSeqNo(1)
                                        .setDataNodeHost(dataNode.getHost())
                                        .setDataNodePort(dataNode.getCommandPort())
                                        .build();

                                dbService.saveObject(chunk);

                                out.writeObject(Status.OK);
                                out.writeObject(dataNode);
                                out.writeObject(filename);
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

    @Bean
    public List<DataNode> getDataNodes() {
        return dataNodes;
    }
}
