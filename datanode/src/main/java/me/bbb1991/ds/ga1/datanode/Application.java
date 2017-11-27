package me.bbb1991.ds.ga1.datanode;

import me.bbb1991.ds.ga1.datanode.service.DataNodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main class
 *
 * @author Bagdat BImaganbetov
 * @author b.bimaganbetov@innopolis.ru
 */
@SpringBootApplication
public class Application {

    /**
     * Class with methods that works with remote server
     */
    private static DataNodeService dataNodeService;

    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    /**
     * main method, that start everything
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        SpringApplication.run(Application.class, args);

        dataNodeService.notifyNameNode();
        dataNodeService.openSocket();
    }

    @Autowired
    public void setDataNodeService(DataNodeService dataNodeService) {
        Application.dataNodeService = dataNodeService;
    }
}
