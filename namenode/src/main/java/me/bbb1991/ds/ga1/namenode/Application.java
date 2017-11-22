package me.bbb1991.ds.ga1.namenode;

import me.bbb1991.ds.ga1.common.model.DataNode;
import me.bbb1991.ds.ga1.namenode.service.NameNodeService;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.ipc.RPC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
@EntityScan("me.bbb1991.ds.ga1.common.model")
public class Application {

    private static NameNodeService nameNodeService;

    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    private static List<DataNode> dataNodes;

    public static void main(String[] args) throws Exception {

        new SpringApplicationBuilder(Application.class).bannerMode(Banner.Mode.OFF).application().run(args);

//        SpringApplication.run(Application.class, args);

        dataNodes = new ArrayList<>();

        Configuration conf = new Configuration();

        RPC.Server server = new RPC.Builder(conf)
                .setProtocol(NameNodeProtocol.class)
                .setInstance(new NameNodeProtocolImpl())
                .setBindAddress("0.0.0.0")
                .setNumHandlers(2)
                .setVerbose(true)
                .setPort(9000)
                .build();
        server.start();
        nameNodeService.openSocketToClient();
        nameNodeService.openSocketToDataNode();
    }

    @Autowired
    public void setNameNodeService(NameNodeService nameNodeService) {
        Application.nameNodeService = nameNodeService;
    }

    @Bean
    public static List<DataNode> getDataNodes() {
        return dataNodes;
    }
}
