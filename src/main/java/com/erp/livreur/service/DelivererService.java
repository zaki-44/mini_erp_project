package com.erp.livreur.service;

import com.erp.livreur.dao.DelivererDAO;
import com.erp.livreur.entity.Deliverer;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.List;

@Stateless
public class DelivererService {

    @Inject
    private DelivererDAO dao;

    public Deliverer addDeliverer(Deliverer d) {
        // Validate fields here if needed
        return dao.create(d);
    }

    public Deliverer getDeliverer(Integer id) {
        return dao.find(id);
    }

    public List<Deliverer> getAllDeliverers() {
        return dao.findAll();
    }

    public Deliverer updateDeliverer(Deliverer d) {
        // Add business logic validations here
        return dao.update(d);
    }

    public void deleteDeliverer(Integer id) {
        dao.delete(id);
    }

    public void changeAvailability(Integer id, boolean available) {
        Deliverer d = dao.find(id);
        if (d != null) {
            d.setAvailable(available);
            dao.update(d);
        }
    }
}
