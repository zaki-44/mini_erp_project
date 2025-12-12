package com.app.service;

import com.app.dao.Implementation.AffectationDAO;
import com.app.dao.Implementation.DelivererDAO;
import com.app.dao.Implementation.DeliveryPackageDAO;

public class DeliveryAssignmentService {
    private DelivererDAO delivererDAO;
    private DeliveryPackageDAO packageDAO;
    private AffectationDAO affectationDAO;
    
    public DeliveryAssignmentService() {
        this.delivererDAO = new DelivererDAO();
        this.packageDAO = new DeliveryPackageDAO();
        this.affectationDAO = new AffectationDAO();
    }

    
}
