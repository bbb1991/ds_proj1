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
                LOGGER.debug("Datanodes count is: {}", dataNodes.size());

                dataNodes.stream()
                        .peek(dataNode -> {
                            try (Socket socket = new Socket(dataNode.getHost(), dataNode.getPort());
                                 ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                                 ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                            ) {
                                LOGGER.debug("Sending request to datanode: {}", dataNode);
                                Thread.sleep(500);
                                out.writeObject(CommandType.HEARTBEAT);
                                Status status = (Status) in.readObject();
                            } catch (Exception e) {
                                LOGGER.error("ERROR!", e);
                                dataNode.setAlive(false);
                            }
                        })
                        .filter(e -> !e.isAlive())
                        .forEach(e -> {
                            LOGGER.info("Datanode: {} is dead. Removing from DB", e);
                            dbService.removeDataNode(e);

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
                                List<String> names = (List<String>) in.readObject();
                                LOGGER.info("Removing folder/file with name: {}", names.stream().reduce(", ", String::concat));
                                names.forEach(e -> {
                                    long id = dbService.getIdByName(e);
                                    List<Chunk> filesToRemove = dbService.getAllChildsById(id);
                                    LOGGER.info("Marked to remove objects: {}", filesToRemove.size());

                                    LOGGER.info(Arrays.deepToString(filesToRemove.toArray()));

                                    if (filesToRemove.stream().anyMatch(el -> el.getDatatype() != FileType.FOLDER)) {

                                        LOGGER.info("Also removing some files...");

                                        dataNodes.forEach(dn -> {
                                            try (Socket socket1 = new Socket(dn.getHost(), dn.getPort());
                                                 ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket1.getOutputStream());
                                                 ObjectInputStream objectInputStream = new ObjectInputStream(socket1.getInputStream());
                                            ) {
                                                objectOutputStream.writeObject(CommandType.REMOVE);
                                                objectOutputStream.writeObject(
                                                        filesToRemove.stream()
                                                                .filter(ff -> !(ff.getDatatype() == FileType.FOLDER))
                                                                .collect(Collectors.toList())
                                                );

                                                Status status = (Status) objectInputStream.readObject();

                                                if (status != Status.OK) {
                                                    LOGGER.warn("Cannot remove some files in datanode: {}:{}", dn.getHost(), dn.getPort());
                                                }

                                            } catch (Exception er) {
                                                er.printStackTrace();
                                            }
                                        });
                                    }

                                    dbService.removeListOfObjects(filesToRemove);
                                });

                                out.writeObject(Status.OK);
                                break;

                            case GET_ID:
                                String name = (String) in.readObject();
                                LOGGER.info("Getting ID by name: {}", name);


                                out.writeObject(Status.OK);
                                out.writeObject(dbService.getIdByName(name));
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
                                dbService.addDataNode(dataNode);
                                dataNodes.add(dataNode);
                                LOGGER.info("{}", dataNode);

                                if (dataNodes.size() > 1) {
                                    try (Socket socket1 = new Socket(dataNodes.get(0).getHost(), dataNodes.get(0).getPort());
                                         ObjectInputStream objectInputStream = new ObjectInputStream(socket1.getInputStream());
                                         ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket1.getOutputStream());
                                    ) {
                                        objectOutputStream.writeObject(CommandType.SYNC);
                                        objectOutputStream.writeObject(dataNode);

                                        Status status = (Status) objectInputStream.readObject();

                                        if (status != Status.OK) {
                                            LOGGER.warn("Sync is failed");
                                        }
                                    }
                                }
                                break;

                            case UPLOADED:
                                filename = (String) in.readObject();
                                LOGGER.info("Unlocking file: {}", filename);
                                dbService.unlockFile(filename);
                                break;

                            default:
                                throw new RuntimeException("Not implemented!");

                        }
                    } catch (Exception e) {
                        LOGGER.error("ERROR!", e);
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
