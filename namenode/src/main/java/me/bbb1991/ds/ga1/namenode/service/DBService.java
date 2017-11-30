package me.bbb1991.ds.ga1.namenode.service;

import me.bbb1991.ds.ga1.common.model.Chunk;
import me.bbb1991.ds.ga1.common.model.DataNode;
import me.bbb1991.ds.ga1.namenode.dao.DataNodeDao;
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

    private DataNodeDao dataNodeDao;


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
    public void setDataNodeDao(DataNodeDao dataNodeDao) {
        this.dataNodeDao = dataNodeDao;
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

    public long getIdByName(String name) {
        return fileChunkDao.getId(name);
    }

    public void rename(String oldName, String newName, long id) {
        fileChunkDao.rename(oldName, newName, id);
    }

    public List<Chunk> getAllChildsById(long id) {
        return fileChunkDao.getAllChildsById(id);
    }

    public void removeListOfObjects(List<Chunk> files) {
        files.forEach(this::removeObject);
    }

    private void removeObject(Chunk e) {
        fileChunkDao.removeObject(e);
    }

    public boolean isDataNodeDoesNotExist() {
        return dataNodeDao.isTableEmpty();
    }

    public List<DataNode> getAllDataNodes() {
        return dataNodeDao.getAll();
    }

    public void addDataNode(DataNode dataNode) {
        dataNodeDao.save(dataNode);
    }

    public void removeDataNode(DataNode dataNode) {
        dataNodeDao.remove(dataNode);
    }

    public void unlockFile(String filename) {
        fileChunkDao.unlockFile(filename);
    }
}
