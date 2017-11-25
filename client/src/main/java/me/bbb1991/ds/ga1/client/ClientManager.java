package me.bbb1991.ds.ga1.client;

import me.bbb1991.ds.ga1.common.model.Chunk;
import me.bbb1991.ds.ga1.common.model.CommandType;
import me.bbb1991.ds.ga1.common.model.DataNode;
import me.bbb1991.ds.ga1.common.model.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.Socket;
import java.util.List;

/**
 * Class with various methods, that will help to communicate client with servers via sockets
 *
 * @author Bagdat Bimaganbetov
 * @author b.bimaganbetov@innopolis.ru
 */
@Component
public class ClientManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientManager.class);

    /**
     * Get list of files from namenode
     *
     * @param path working directory
     * @return a list of files from {@param path}
     */
    public List<Chunk> getListOfFiles(String path) {
        LOGGER.info("Sending command to get list of files to name node");
        try (Socket socket = new Socket("localhost", 9001);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            out.writeObject(CommandType.LIST_FILES);
            LOGGER.info("Sent command");
            out.writeObject(path);
            LOGGER.info("Sent path info");
            Status status = (Status) in.readObject();

            LOGGER.info("Response status is: {}", status);

            if (status != Status.OK) {
                throw new RuntimeException();
            }

            return (List<Chunk>) in.readObject();


        } catch (Exception e) {
            LOGGER.error("ERROR!", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Download file from datanode by given {@param file}
     *
     * @param file filename to download
     * @return in case if file was splitted into chunks will return list of chunks, else list with single element
     */
    public List<Chunk> getFile(String file) {
        LOGGER.info("Sending request to name node to get info about where we can download given file");
        try (Socket socket = new Socket("localhost", 9001);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            out.writeObject(CommandType.GET);
            LOGGER.info("Sent command");
            out.writeObject(file);
            LOGGER.info("Sent path");
            Status status = (Status) in.readObject();

            LOGGER.info("Response status is: {}", status);

            if (status != Status.OK) {
                throw new RuntimeException();
            }

            return (List<Chunk>) in.readObject();


        } catch (Exception e) {
            LOGGER.error("ERROR!", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Create a folder in namenode.
     *
     * @param folderName name of new folder
     */
    public void mkdir(String folderName) { // TODO add current path
        // TODO change hardcoded address to config
        LOGGER.info("Sending request to create folder");
        try (Socket socket = new Socket("localhost", 9001);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            out.writeObject(CommandType.MKDIR);
            out.writeObject(folderName);
            Status status = (Status) in.readObject();

            LOGGER.info("Response status is: {}", status);

            if (status != Status.OK) {
                throw new RuntimeException();
            }

        } catch (Exception e) {
            LOGGER.error("ERROR!", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Upload file to datanode
     *
     * @param multipartFile to upload to remote server
     */
    public void uploadFile(MultipartFile multipartFile) {
        // todo change hardcoded address
        // todo add logic to work with folders
        try (Socket socket = new Socket("localhost", 9001);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            // send request to name node
            out.writeObject(CommandType.UPLOAD_FILE);
            out.writeObject(multipartFile.getSize());
            out.writeObject(multipartFile.getOriginalFilename());
//            out.writeObject(convert(file));
            Status status = (Status) in.readObject();
            LOGGER.info("Uploading multipart file: {}", multipartFile.getOriginalFilename());

            if (status != Status.OK) {
                throw new RuntimeException();
            }

            DataNode datanode = (DataNode) in.readObject();
            String filename = (String) in.readObject();

            LOGGER.info("Response status is: {}", status);
            LOGGER.info("Datanode is: {}", datanode);
            LOGGER.info("File name is: {}", filename);

            sendFile(datanode.getHost(), datanode.getCommandPort(), multipartFile, filename);

        } catch (Exception e) {
            LOGGER.error("ERROR!", e);
            throw new RuntimeException(e);
        }
    }

    private void sendFile(String host, int port, MultipartFile file, String filename) throws IOException {
        try (Socket socket = new Socket(host, port);
             DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())
        ) {
            out.writeObject(CommandType.UPLOAD_FILE);
            out.writeObject(file.getSize());
            out.writeObject(filename);

            byte[] bytes = (file.getBytes());
            dataOutputStream.write(bytes);
            dataOutputStream.flush();

            Status status = (Status) in.readObject();
            if (status != Status.OK) {
                throw new RuntimeException("Status is not what we expected");
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
