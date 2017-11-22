package me.bbb1991.ds.ga1.namenode.service;

import me.bbb1991.ds.ga1.common.model.Chunk;
import me.bbb1991.ds.ga1.namenode.dao.FileChunkDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DBService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DBService.class);

    private FileChunkDao fileChunkDao;


    public List<Chunk> getAllFiles(String folder) {
        return fileChunkDao.getAllFilesOnFolder(folder);
    }

    @Autowired
    public void setFileChunkDao(FileChunkDao fileChunkDao) {
        this.fileChunkDao = fileChunkDao;
    }

    public List<Chunk> getFilesByName(String fileName) {
        return fileChunkDao.getFileByName(fileName);
    }

    @Transactional
    public void saveObject(Chunk chunk) {
        fileChunkDao.saveFile(chunk);
        LOGGER.info("Object {} seems to be saved. Let's try!", chunk);
        Chunk savedChunk = fileChunkDao.getAllFilesOnFolder("/").get(0);
        LOGGER.info("Check is saved: {}", savedChunk);
    }
}
