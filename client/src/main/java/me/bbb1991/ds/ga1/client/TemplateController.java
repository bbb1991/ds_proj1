package me.bbb1991.ds.ga1.client;

import me.bbb1991.ds.ga1.common.model.Chunk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Controller
public class TemplateController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TemplateController.class);

    private ClientManager clientManager;

    @GetMapping("/")
    public String index(Map<String, Object> model) {

        List<Chunk> files = clientManager.getListOfFiles("/");

        LOGGER.info("Got result: {}", files);

        model.put("files", files);
        return "index";
    }

    @PostMapping("/mkdir")
    public String mkdir(@RequestParam String folderName) {

        LOGGER.info("Incoming message for creating folder. Folder name is: {}", folderName);

        clientManager.mkdir(folderName);

        return "redirect:/";
    }

    @Autowired
    public void setClientManager(ClientManager clientManager) {
        this.clientManager = clientManager;
    }
}
