package me.bbb1991.ds.ga1.client;

import me.bbb1991.ds.ga1.common.model.Chunk;
import me.bbb1991.ds.ga1.common.model.CommandType;
import me.bbb1991.ds.ga1.common.model.Container;
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

@Component
public class ClientManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientManager.class);

    public List<Chunk> getListOfFiles(String path) {
        LOGGER.info("Sending command to get list of files to name node");
        return executor(null, new Container<>(CommandType.LIST_FILES, path));
    }

    public List<Chunk> getFile(String file) {
        LOGGER.info("Sending request to name node to get info about where we can download given file");
        return executor(null, new Container(CommandType.GET, file));
    }

    public void mkdir(String folderName) {
        LOGGER.info("Sending request to create folder");
        executor(Void.class, new Container<>(CommandType.MKDIR, folderName));
    }


    public <T> T executor(T returnType, Container container) {
        try (Socket socket = new Socket("localhost", 9001);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            out.writeObject(container);
            Container<T> result = (Container<T>) in.readObject();

            LOGGER.info("The result of request is: {}", result);
            if (result.getStatus() != CommandType.OK) {
                throw new RuntimeException("Response is not that we expected!");
            }
            return result.getObject();
        } catch (Exception e) {
            LOGGER.error("ERROR!", e);
            throw new RuntimeException(e);
        }
    }

    public void uploadFile(MultipartFile file) {
        executor(Void.class, new Container<>(CommandType.UPLOAD_FILE, convert(file)));
    }

    public static File convert(MultipartFile file) {
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
