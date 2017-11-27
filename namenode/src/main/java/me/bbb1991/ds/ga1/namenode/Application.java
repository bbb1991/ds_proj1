package me.bbb1991.ds.ga1.namenode;

import me.bbb1991.ds.ga1.namenode.service.NameNodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * Main class that runs everything
 *
 * @author Bagdat Bimaganbetov
 * @author b.bimaganbetov@innopolis.ru
 */
@SpringBootApplication
@EntityScan("me.bbb1991.ds.ga1.common.model")
public class Application {

    /**
     * Class with various methods to connect datanode and client.
     */
    private static NameNodeService nameNodeService;

    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) throws Exception {

        new SpringApplicationBuilder(Application.class).bannerMode(Banner.Mode.OFF).application().run(args);

        nameNodeService.openSocketToClient();
    }

    @Autowired
    public void setNameNodeService(NameNodeService nameNodeService) {
        Application.nameNodeService = nameNodeService;
    }
}
