package me.bbb1991.ds.ga1.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

    private static ClientManager clientManager;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
        clientManager.getListOfMessages();
    }

    @Autowired
    public void setClientManager(ClientManager clientManager) {
        Application.clientManager = clientManager;
    }
}
