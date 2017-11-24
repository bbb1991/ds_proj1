package me.bbb1991.ds.ga1.namenode;

import me.bbb1991.ds.ga1.namenode.service.NameNodeService;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.ipc.RPC;
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
     * What host should listen
     */
    private static String nameNodeHost;

    /**
     * What port should listen
     */
    private static int nameNodePort;

    /**
     * Class with various methods to connect datanode and client.
     */
    private static NameNodeService nameNodeService;

    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) throws Exception {

        new SpringApplicationBuilder(Application.class).bannerMode(Banner.Mode.OFF).application().run(args);

        Configuration conf = new Configuration();

        RPC.Server server = new RPC.Builder(conf)
                .setProtocol(NameNodeProtocol.class)
                .setInstance(new NameNodeProtocolImpl())
                .setBindAddress(nameNodeHost)
                .setNumHandlers(2)
                .setVerbose(true)
                .setPort(nameNodePort)
                .build();
        server.start();
        nameNodeService.openSocketToClient();
        nameNodeService.openSocketToDataNode();
    }

    @Autowired
    public void setNameNodeService(NameNodeService nameNodeService) {
        Application.nameNodeService = nameNodeService;
    }

    @Value("${namenode.host}")
    public void setNameNodeHost(String nameNodeHost) {
        Application.nameNodeHost = nameNodeHost;
    }

    @Value("${namenode.port}")
    public void setNameNodePort(int nameNodePort) {
        Application.nameNodePort = nameNodePort;
    }
}
