package me.bbb1991.ds.ga1.client;

import me.bbb1991.ds.ga1.common.model.Chunk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * Class with controllers to server HTTP requests.
 */
@Controller
public class TemplateController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TemplateController.class);

    /**
     * Class holder with various methods to work with remote servers
     */
    private ClientManager clientManager;


    /**
     * Return main HTML page
     *
     * @param model variable for moving
     * @return name of HTML page. Always will return <code>index</code>
     */
    @GetMapping("/")
    public String index(Map<String, Object> model) {

        LOGGER.info("Incoming message for for get incoming message.");

        List<Chunk> files = clientManager.getListOfFiles("/");

        LOGGER.info("Got result: {}", files);

        model.put("files", files);
        return "index";
    }

    /**
     * Create folder with given name.
     *
     * @param folderName what name is you given
     * @return redirect to main page
     * @see TemplateController#index(Map)
     */
    @PostMapping("/mkdir")
    public String mkdir(@RequestParam String folderName) {

        LOGGER.info("Incoming message for creating folder. Folder name is: {}", folderName);

        clientManager.mkdir(folderName);

        return "redirect:/";
    }

    /**
     * Uploading new file to the datanode
     *
     * @param file to upload
     * @return redirect to main page.
     * @see TemplateController#index(Map)
     */
    @PostMapping("/upload")
    public String upload(@RequestParam MultipartFile file) {

        LOGGER.info("Incoming request for uploading file: {}", file);

        clientManager.uploadFile(file);

        return "redirect:/";
    }

    @Autowired
    public void setClientManager(ClientManager clientManager) {
        this.clientManager = clientManager;
    }
}
