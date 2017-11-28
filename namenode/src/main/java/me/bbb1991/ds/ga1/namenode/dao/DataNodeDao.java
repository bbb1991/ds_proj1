package me.bbb1991.ds.ga1.namenode.dao;

import me.bbb1991.ds.ga1.common.model.DataNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
public class DataNodeDao {

    private EntityManager entityManager;

    @Transactional
    public void save(DataNode dataNode) {
        entityManager.persist(dataNode);
    }

    @Transactional
    public void remove(DataNode dataNode) {
        entityManager.remove(entityManager.contains(dataNode) ? dataNode : entityManager.merge(dataNode));
    }

    @SuppressWarnings("unchecked")
    public List<DataNode> getAll() {
        return entityManager.createQuery("select dn from DataNode dn").getResultList();
    }

    @Autowired
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public boolean isTableEmpty() {
        return this.getAll().isEmpty();
    }
}
