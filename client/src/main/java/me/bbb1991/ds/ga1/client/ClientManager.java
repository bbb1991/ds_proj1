package me.bbb1991.ds.ga1.client;

import me.bbb1991.ds.ga1.common.model.Chunk;
import me.bbb1991.ds.ga1.common.model.CommandType;
import me.bbb1991.ds.ga1.common.model.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
     * @param file to upload to remote server
     */
    public void uploadFile(MultipartFile file) {
        // todo change hardcoded address
        // todo add logic to work with folders
        try (Socket socket = new Socket("localhost", 9001);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            out.writeObject(CommandType.UPLOAD_FILE);
            out.writeObject(file.getSize());
            out.writeObject(file.getName());
//            out.writeObject(convert(file));
            Status status = (Status) in.readObject();
            String datanode = (String) in.readObject();
            String filename = (String) in.readObject();

            LOGGER.info("Response status is: {}", status);
            LOGGER.info("Datanode is: {}", datanode);
            LOGGER.info("File name is:{}", filename);

            if (status != Status.OK) {
                throw new RuntimeException();
            }

        } catch (Exception e) {
            LOGGER.error("ERROR!", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Convert file from {@link MultipartFile} to {@link File}
     *
     * @param file that need to upload
     * @return converted file
     */
    private static File convert(MultipartFile file) {
        File convFile = new File(file.getOriginalFilename());

        try (FileOutputStream fos = new FileOutputStream(convFile)) {
            convFile.createNewFile();
            fos.write(file.getBytes());
            return convFile;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
