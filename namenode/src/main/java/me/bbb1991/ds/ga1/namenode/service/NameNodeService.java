package me.bbb1991.ds.ga1.namenode.service;

import me.bbb1991.ds.ga1.common.Utils;
import me.bbb1991.ds.ga1.common.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${namenode.port}")
    private int namenodePort;

    public void openSocketToClient() {
        final ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(namenodePort);
            new Thread(() -> {
                while (true) {
                    try (Socket socket = serverSocket.accept();
                         ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                         ObjectInputStream in = new ObjectInputStream(socket.getInputStream())
                    ) {
                        CommandType command = (CommandType) in.readObject();
                        LOGGER.info("Got command from client: {}", command);
                        switch (command) {
                            case LIST_FILES:
                                LOGGER.info("Getting path info");
                                String path = (String) in.readObject();
                                LOGGER.info("Got path info. Forming answer");
                                List<Chunk> files = dbService.getAllFiles(path);
                                LOGGER.info("Files: {}", files.size());
                                out.writeObject(Status.OK);
                                out.writeObject(files);
                                break;

                            case GET:
                                String fileName = (String) in.readObject();
                                List<Chunk> f  = dbService.getFilesByName(fileName);
                                LOGGER.info("Getting filename: {}", fileName);
                                out.writeObject(Status.OK);
                                out.writeObject(f);
                                break;


                            case MKDIR:
                                Chunk chunk = (Chunk) in.readObject();
                                LOGGER.info("Creating folder with name: {}", chunk.getOriginalName());

                                dbService.saveObject(chunk);
                                out.writeObject(Status.OK);
                                break;

                            case UPLOAD_FILE:
                                chunk = (Chunk) in.readObject();
                                LOGGER.info("Original name is: {}", chunk.getOriginalName());
                                if (dataNodes.isEmpty()) {
                                    LOGGER.warn("No data node available to save file!");
                                    out.writeObject(Status.NO_DATANODE_AVAILABLE);
                                    break;
                                }

                                DataNode dataNode = dataNodes.get(0);

                                String filename = Utils.getFileName();
                                LOGGER.info("File name is: {}", filename);

                                chunk.setFilename(filename);
                                chunk.setLocked(true);
                                chunk.setDataNodeHost(dataNode.getHost());
                                        chunk.setDataNodePort(dataNode.getCommandPort());

                                dbService.saveObject(chunk);

                                out.writeObject(Status.OK);
                                out.writeObject(dataNode);
                                out.writeObject(filename);
                                break;

                            case REMOVE:
                                String name = (String) in.readObject();
                                LOGGER.info("Creating folder with name: {}", name);

                                dbService.removeObject(name);
                                out.writeObject(Status.OK);
                                break;

                            case GET_ID:
                                name = (String) in.readObject();
                                LOGGER.info("Getting ID by name: {}", name);


                                out.writeObject(Status.OK);
                                out.writeObject( dbService.getIdByName(name));
                                break;

                            case RENAME:
                                String oldName = (String) in.readObject();
                                String newName = (String) in.readObject();
                                long id = (long) in.readObject();

                                dbService.rename(oldName, newName, id);
                                out.writeObject(Status.OK);
                                break;

                            case HELLO:
                                dataNode = (DataNode) in.readObject();
                                LOGGER.info("New Data Node came up!: {}", dataNode);
                                dataNodes.add(dataNode);
                                LOGGER.info("{}", dataNode);
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
