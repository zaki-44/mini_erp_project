package com.erp.livreur.dao;

import com.erp.livreur.entity.Deliverer;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Stateless
public class DelivererDAO {

    @PersistenceContext(unitName = "erpPU")
    private EntityManager em;

    public Deliverer create(Deliverer deliverer) {
        em.persist(deliverer);
        return deliverer;
    }

    public Deliverer find(Integer id) {
        return em.find(Deliverer.class, id);
    }

    public Deliverer update(Deliverer deliverer) {
        return em.merge(deliverer);
    }

    public void delete(Integer id) {
        Deliverer d = em.find(Deliverer.class, id);
        if (d != null) {
            em.remove(d);
        }
    }

    public List<Deliverer> findAll() {
        return em.createQuery("SELECT d FROM Deliverer d", Deliverer.class).getResultList();
    }
}