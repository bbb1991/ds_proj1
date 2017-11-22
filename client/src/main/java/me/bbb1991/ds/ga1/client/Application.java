package me.bbb1991.ds.ga1.client;

import me.bbb1991.ds.ga1.common.model.Chunk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

@SpringBootApplication
public class Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    private static ClientManager clientManager;

    public static void main(String[] args) throws IOException {
        SpringApplication.run(Application.class, args);
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            System.out.print("input> ");
            String userInp = reader.readLine();
            if ("exit".equalsIgnoreCase(userInp)) {
                break;
            }

            String command = userInp.split("\\s")[0];
            LOGGER.info("You entered command: {}", command);
            switch (command) {
                case "ls":
                    List<Chunk> files = clientManager.getListOfFiles("/");
                    LOGGER.info("Files: {}", Arrays.deepToString(files.toArray()));
                    break;
                case "get":
                    LOGGER.info("Getting file: {}", userInp.split("\\s")[1]);
                    List<Chunk> f = clientManager.getFile(userInp.split("\\s")[1]);
                    System.out.println(f);
                    break;
                default:
                    LOGGER.warn("Container {} does not recognised! Try again!", command);
                    break;
            }
        }
    }

    @Autowired
    public void setClientManager(ClientManager clientManager) {
        Application.clientManager = clientManager;
    }
}
