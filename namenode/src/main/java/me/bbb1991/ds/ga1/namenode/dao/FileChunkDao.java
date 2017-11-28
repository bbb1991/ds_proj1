package me.bbb1991.ds.ga1.namenode.dao;

import me.bbb1991.ds.ga1.common.model.Chunk;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class FileChunkDao {

    private EntityManager entityManager;

    @SuppressWarnings("unchecked")
    public List<Chunk> getAllFilesOnFolder(String folder) {
        long parentId = this.getId(folder);
        Query query = entityManager.createQuery("select c from Chunk c where c.parentId = :parentId");
        query.setParameter("parentId", parentId);
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
        Query query = entityManager.createQuery("select c from Chunk c where c.originalName = :filename");
        query.setParameter("filename", fileName);

        return query.getResultList();
    }

    @Transactional
    public void removeObject(String name) {
        entityManager.remove(this.getFileByName(name).get(0));
    }

    public long getId(String name) {
        Query query = entityManager.createQuery("select c from Chunk c where c.originalName = :name");
        query.setParameter("name", name);

        Chunk chunk = (Chunk) query.getSingleResult();
        return chunk.getId();
    }

    @Transactional
    public void rename(String oldName, String newName, long id) {
        Query query = entityManager.createQuery("update Chunk c set c.originalName=:newName where c.originalName=:oldName and c.parentId=:id")
                .setParameter("oldName", oldName)
                .setParameter("newName", newName)
                .setParameter("id", id);

        query.executeUpdate();
    }

    @SuppressWarnings("unchecked")
    public List<Chunk> getAllChildsById(long id) {
        Query nativeQuery = entityManager.createNativeQuery("WITH RECURSIVE T(id,  parent_id) AS (" +
                "    SELECT id, 0 AS parent_id " +
                "    FROM chunk WHERE id = :id " +
                "    UNION ALL " +
                "    SELECT ou.id, ou.parent_id " +
                "    FROM T INNER JOIN chunk AS ou ON T.id = ou.parent_id " +
                ") SELECT id FROM T ");
        nativeQuery.setParameter("id", id);

        List ids = (List) nativeQuery.getResultList().stream()
                .map(e -> Long.valueOf(String.valueOf(e).replaceAll("\\[", "").replaceAll("]", "")))
                .collect(Collectors.toList());

        return entityManager.createQuery("select c from Chunk c where c.id in (?1)").setParameter(1, ids).getResultList();
    }

    @Transactional
    public void removeObject(Chunk e) {
        entityManager.remove(entityManager.contains(e) ? e : entityManager.merge(e));
    }
}
