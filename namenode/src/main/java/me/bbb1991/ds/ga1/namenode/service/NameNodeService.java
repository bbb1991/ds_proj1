package me.bbb1991.ds.ga1.namenode.service;

import me.bbb1991.ds.ga1.common.Utils;
import me.bbb1991.ds.ga1.common.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class NameNodeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NameNodeService.class);

    private DBService dbService;

    @Value("${namenode.port}")
    private int namenodePort;

    public void heartBeat() {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(5_000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (dbService.isDataNodeDoesNotExist()) {
                    LOGGER.debug("No datanode available, so sad :(");
                    continue;
                }

                List<DataNode> dataNodes = dbService.getAllDataNodes();

                dataNodes.stream()
                        .peek(dataNode -> {
                            try (Socket socket = new Socket(dataNode.getHost(), dataNode.getPort());
                                 ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                                 ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                            ) {
                                Thread.sleep(500);
                                out.writeObject(CommandType.HEARTBEAT);
                                in.readObject();
                            } catch (Exception e) {
                                dataNode.setAlive(false);
                            }

                        })
                        .filter(DataNode::isAlive)
                        .forEach(dataNode -> {
                            LOGGER.info("Datanode: {}:{} is dead. Removing from DB");
                            dbService.removeDataNode(dataNode);

                        });
            }
        }).start();
    }

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
                        List<DataNode> dataNodes = dbService.getAllDataNodes();
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
                                if (dataNodes.isEmpty()) {
                                    LOGGER.warn("No datanode available to serve client!");
                                    out.writeObject(Status.NO_DATANODE_AVAILABLE);
                                    break;
                                }


                                String fileName = (String) in.readObject();
                                List<Chunk> f = dbService.getFilesByName(fileName);
                                f = f.stream()
                                        .peek(c -> {
                                            c.setDataNodeHost(dataNodes.get(0).getHost());
                                            c.setDataNodePort(dataNodes.get(0).getPort());
                                        })
                                        .collect(Collectors.toList());
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

                                dbService.saveObject(chunk);

                                out.writeObject(Status.OK);
                                out.writeObject(dataNode);
                                out.writeObject(filename);
                                break;

                            case REMOVE:
                                String name = (String) in.readObject();
                                LOGGER.info("Removing folder/file with name: {}", name);

                                long id = dbService.getIdByName(name);
                                files = dbService.getAllChildsById(id);
                                LOGGER.info("Marked to remove objects: {}", files.size());

                                LOGGER.info(Arrays.deepToString(files.toArray()));

                                if (files.stream().anyMatch(e -> e.getDatatype() != FileType.FOLDER)) {

                                    LOGGER.info("Also removing some files...");

                                    dataNodes.forEach(dn -> {
                                        try (Socket socket1 = new Socket(dn.getHost(), dn.getPort());
                                             ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket1.getOutputStream());
                                             ObjectInputStream objectInputStream = new ObjectInputStream(socket1.getInputStream());
                                        ) {
                                            objectOutputStream.writeObject(CommandType.REMOVE);
                                            objectOutputStream.writeObject(
                                                    files.stream()
                                                            .filter(file -> !(file.getDatatype() == FileType.FOLDER))
                                                            .collect(Collectors.toList())
                                            );

                                            Status status = (Status) objectInputStream.readObject();

                                            if (status != Status.OK) {
                                                LOGGER.warn("Cannot remove some files in datanode: {}:{}", dn.getHost(), dn.getPort());
                                            }

                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    });
                                }

                                dbService.removeListOfObjects(files);
                                out.writeObject(Status.OK);
                                break;

                            case GET_ID:
                                name = (String) in.readObject();
                                LOGGER.info("Getting ID by name: {}", name);


                                out.writeObject(Status.OK);
                                out.writeObject(dbService.getIdByName(name));
                                break;

                            case RENAME:
                                String oldName = (String) in.readObject();
                                String newName = (String) in.readObject();
                                id = (long) in.readObject();

                                dbService.rename(oldName, newName, id);
                                out.writeObject(Status.OK);
                                break;

                            case HELLO:
                                dataNode = (DataNode) in.readObject();
                                LOGGER.info("New Data Node came up!: {}", dataNode);
                                dbService.addDataNode(dataNode);
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
}
