package me.bbb1991.ds.ga1.datanode;

import me.bbb1991.ds.ga1.common.Utils;
import me.bbb1991.ds.ga1.datanode.service.DataNodeService;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.ipc.RPC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

    private static DataNodeService dataNodeService;

    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) throws Exception {

        SpringApplication.run(Application.class, args);

        int port = Utils.getRandomPort();

        Configuration conf = new Configuration();

        RPC.Server server = new RPC.Builder(conf)
                .setProtocol(DataNodeProtocol.class)
                .setInstance(new DataNodeProtocolImpl())
                .setBindAddress("0.0.0.0")
                .setNumHandlers(2)
                .setPort(port)
                .setVerbose(true)
                .build();
        server.start();

        dataNodeService.notifyNameNode();
        dataNodeService.openSocket();

//        server.stop();
    }

    @Autowired
    public void setDataNodeService(DataNodeService dataNodeService) {
        Application.dataNodeService = dataNodeService;
    }
}
