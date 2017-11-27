package me.bbb1991.ds.ga1.namenode.service;

import me.bbb1991.ds.ga1.common.model.Chunk;
import me.bbb1991.ds.ga1.namenode.dao.FileChunkDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Class with methods to work with DB
 *
 * @author Bagdat Bimaganbetov
 * @author b.bimaganbetov@innopolis.ru
 */
@Service
public class DBService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DBService.class);

    private FileChunkDao fileChunkDao;


    /**
     * Get files and folders in given {@param folder}
     *
     * @param folder in what folder we should look
     * @return a list of files
     */
    public List<Chunk> getAllFiles(String folder) {
        return fileChunkDao.getAllFilesOnFolder(folder);
    }

    @Autowired
    public void setFileChunkDao(FileChunkDao fileChunkDao) {
        this.fileChunkDao = fileChunkDao;
    }

    /**
     * Get file by name. If file was split into chunks returns list of chunks
     *
     * @param fileName what file we are looking
     * @return list of chunks or list with single file
     */
    public List<Chunk> getFilesByName(String fileName) {
        return fileChunkDao.getFileByName(fileName);
    }

    /**
     * Save record about file/folder to DB
     *
     * @param chunk what we should save
     */
    @Transactional
    public void saveObject(Chunk chunk) {
        fileChunkDao.saveFile(chunk);
        LOGGER.info("Object {} seems to be saved. Let's try!", chunk);
        LOGGER.info("Check is saved: {}", chunk);
    }

    public void removeObject(String name) {
        fileChunkDao.removeObject(name);
    }

    public long getIdByName(String name) {
        return fileChunkDao.getId(name);
    }
}
