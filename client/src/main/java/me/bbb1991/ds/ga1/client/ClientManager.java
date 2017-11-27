package me.bbb1991.ds.ga1.client;

import me.bbb1991.ds.ga1.common.model.*;
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
    @SuppressWarnings("unchecked")
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
    @SuppressWarnings("unchecked")
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
    public void mkdir(String folderName, long parentId) { // TODO add current path
        // TODO change hardcoded address to config
        LOGGER.info("Sending request to create folder");
        try (Socket socket = new Socket("localhost", 9001);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            Chunk chunk = Chunk.builder().setDatatype(FileType.FOLDER).setOriginalName(folderName).setParentId(parentId).build();
            out.writeObject(CommandType.MKDIR);
            out.writeObject(chunk);
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
    public void uploadFile(MultipartFile multipartFile, long parentId) {
        // todo change hardcoded address
        try (Socket socket = new Socket("localhost", 9001);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            // send request to name node
            Chunk chunk = Chunk.builder().setFileSize(multipartFile.getSize())
                    .setOriginalName(multipartFile.getOriginalFilename())
                    .setParentId(parentId)
                    .setSeqNo(1)
                    .setDatatype(FileType.FILE)
                    .build();
            out.writeObject(CommandType.UPLOAD_FILE);
            out.writeObject(chunk);
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

    public File downloadFile(String host, int port, String fileName, long fileSize) {
        try (Socket socket = new Socket(host, port);
             DataInputStream dataInputStream = new DataInputStream(socket.getInputStream()); // to get a file
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream()); // to get ojects, ex: status
             ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream()); // send objects, ex: status
        ) {
            objectOutputStream.writeObject(CommandType.GET);
            objectOutputStream.writeObject(fileName);

            Status status = (Status) objectInputStream.readObject();
            LOGGER.info("Status is: {}", status);

            if (status != Status.OK) {
                throw new RuntimeException("Status is not OK!");
            }

            File file = File.createTempFile(fileName, ".tmp");
            try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {

                byte[] buffer = new byte[4096];
                int read;
                long totalRead = 0;
                long remaining = fileSize;

                while (((read = dataInputStream.read(buffer, 0, Math.min(buffer.length, (int) remaining))) > 0)) {
                    totalRead += read;
                    remaining -= read;
                    LOGGER.info("Read {} bytes", totalRead);
                    fileOutputStream.write(buffer, 0, read);

                }
                file.deleteOnExit();
                return file;
            }
        } catch (Exception ex) {
            LOGGER.error("Error while downloading file {}!", fileName, ex);
            throw new RuntimeException(ex);
        }
    }

    public void remove(String name) {
        LOGGER.info("Sending request to remove");
        try (Socket socket = new Socket("localhost", 9001);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            out.writeObject(CommandType.REMOVE);
            out.writeObject(name);
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

    public long getFileId(String s) {
        LOGGER.info("Sending request to remove");
        try (Socket socket = new Socket("localhost", 9001);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            out.writeObject(CommandType.GET_ID);
            out.writeObject(s);
            Status status = (Status) in.readObject();

            LOGGER.info("Response status is: {}", status);

            if (status != Status.OK) {
                throw new RuntimeException();
            }

            return (long) in.readObject();

        } catch (Exception e) {
            LOGGER.error("ERROR!", e);
            throw new RuntimeException(e);
        }
    }

    public void renameFile(String oldName, String newName, long id) {

        LOGGER.info("Renaming file from {} to {} in folder {}", oldName, newName, id);

        try (Socket socket = new Socket("localhost", 9001);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            out.writeObject(CommandType.RENAME);
            out.writeObject(oldName);
            out.writeObject(newName);
            out.writeObject(id);
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
}
