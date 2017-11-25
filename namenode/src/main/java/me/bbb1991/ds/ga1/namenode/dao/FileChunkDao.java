package me.bbb1991.ds.ga1.namenode.dao;

import me.bbb1991.ds.ga1.common.model.Chunk;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

@Repository
public class FileChunkDao {

    private EntityManager entityManager;

    @SuppressWarnings("unchecked")
    public List<Chunk> getAllFilesOnFolder(String folder) {
        Query query = entityManager.createQuery("select c from Chunk c"); // TODO filter by folder
        return query.getResultList();
    }

    public void saveFile(Chunk chunk) {
        entityManager.persist(chunk);
    }

    @Autowired
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @SuppressWarnings("unchecked")
    public List<Chunk> getFileByName(String fileName) {
        Query query =  entityManager.createQuery("select c from Chunk c where c.originalName = :filename");
        query.setParameter("filename", fileName);

        return query.getResultList();
    }
}
