package com.app.service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import com.app.dao.Implementation.AffectationDAO;
import com.app.dao.Implementation.DelivererDAO;
import com.app.dao.Implementation.DeliveryPackageDAO;
import com.app.model.DeliveryPackage;
import com.app.model.Enums.AffectationStatus;
import com.app.model.Enums.PackageStatus;
import com.app.model.Enums.VehicleType;
import com.app.model.Affectation;
import com.app.model.Client;
import com.app.model.Deliverer;

public class DeliveryAssignmentService {
    private DelivererDAO delivererDAO;
    private DeliveryPackageDAO packageDAO;
    private AffectationDAO affectationDAO;
    
    private static final double VEHICLE_MATCH_WEIGHT = 40.0;
    private static final double WORKLOAD_PENALTY_PER_ASSIGNMENT = 10.0;
    private static final double PERFORMANCE_BONUS_MAX = 20.0;
    private static final double EXPERIENCE_PER_DELIVERY = 2.0;
    // Vehicle compatibility matrix
    private static final Map<VehicleType, List<VehicleType>> DELIVERER_CAPABILITIES =
    Map.of(
        VehicleType.BIKE,  List.of(VehicleType.BIKE),
        VehicleType.CAR,   List.of(VehicleType.BIKE, VehicleType.CAR),
        VehicleType.TRUCK, List.of(VehicleType.BIKE, VehicleType.CAR, VehicleType.TRUCK)
    );


    public DeliveryAssignmentService() {
        this.delivererDAO = new DelivererDAO();
        this.packageDAO = new DeliveryPackageDAO();
        this.affectationDAO = new AffectationDAO();
    }
    
    private double calculateVehicleScore(Deliverer deliverer, DeliveryPackage pkg) {
        VehicleType needed = pkg.getVehicleTypeNeeded();
        VehicleType has = deliverer.getVehicleType();
        if (needed == null) {
            return 20.0;
        }
        if(has == null){
            return -100.0;
        }
        // HARD DISQUALIFICATION
        if (!DELIVERER_CAPABILITIES.get(has).contains(needed)) {
            return -100.0;
        }

        // Perfect match
        if (has == needed) {
            return VEHICLE_MATCH_WEIGHT;
        }

        // Oversized vehicle penalties (inefficiency)
        if (has == VehicleType.TRUCK && needed != VehicleType.TRUCK) {
            return VEHICLE_MATCH_WEIGHT * 0.4;
        }

        if (has == VehicleType.CAR && needed == VehicleType.BIKE) {
            return VEHICLE_MATCH_WEIGHT * 0.6;
        }

        return VEHICLE_MATCH_WEIGHT * 0.5;
    }
    private double calculateWeightCapacityScore(Deliverer deliverer, DeliveryPackage pkg) {
        if (pkg.getWeight() <= 0 || deliverer.getMaxWeight() <= 0) {
            return 0.0; // No weight data
        }
        
        double weightRatio = pkg.getWeight() / deliverer.getMaxWeight();
        
        if (weightRatio > 1.0) {
            return -100.0; // Package too heavy - DISQUALIFY
        } else if (weightRatio > 0.8) {
            return 25.0; // Heavy load (80-100% capacity) - optimal match
        } else if (weightRatio > 0.5) {
            return 15.0; // Moderate load (50-80% capacity) - good match
        } else if (weightRatio > 0.2) {
            return 5.0; // Light load (20-50% capacity) - ok match
        } else {
            return -5.0; // Very light load (<20%) - inefficient use
        }
    }
    private double calculateWorkloadPenalty(List<Affectation> allAffectations){
        int activeAssignments = 0;

        for (Affectation aff : allAffectations) {
            if (aff.getStatus() == AffectationStatus.PENDING || 
                 aff.getStatus() == AffectationStatus.ACCEPTED) {
                activeAssignments++;
            }
        }
        
        return activeAssignments * WORKLOAD_PENALTY_PER_ASSIGNMENT;
    }
    private double calculateExperienceBonus(List<Affectation> allAffectations) {
        try{
            int completedAssignments = 0;
            for (Affectation aff : allAffectations) {
                if (aff.getStatus() == AffectationStatus.COMPLETED) {
                    completedAssignments++;
                }
            }
            return Math.min(completedAssignments * EXPERIENCE_PER_DELIVERY, PERFORMANCE_BONUS_MAX);
        }
        catch(Exception e){
            System.out.println("Error calculating experience bonus: " + e.getMessage());
            return 0.0;
        }
    }
    private boolean isSameCity(Deliverer d, Client c){
        return d.getCity().equals(c.getCity());
    }

    public double calculateScore(Deliverer deliverer, DeliveryPackage pkg , List<Affectation> affs) {
        try {
            double vehicleScore = calculateVehicleScore(deliverer, pkg);
            if (vehicleScore < 0) return Double.NEGATIVE_INFINITY;

            double weightScore = calculateWeightCapacityScore(deliverer, pkg);
            if (weightScore < -100.0) return Double.NEGATIVE_INFINITY;
            double score = 0.0;
            Client sourceClient = affectationDAO.getSourceClient(pkg);
            if(!isSameCity(deliverer, sourceClient)){ 
                return Double.NEGATIVE_INFINITY; 
            }
            score += vehicleScore;
            score += weightScore;
            score -= calculateWorkloadPenalty(affs);
            score += calculateExperienceBonus(affs);

            return score;

        } catch (Exception e) {
            System.out.println("Error calculating score for deliverer ID " + deliverer.getId() + ": " + e.getMessage());
            return Double.NEGATIVE_INFINITY;
        }
    }
    
    public Deliverer findBestDelivererForPackage(List<Deliverer> potentialDeliverers, DeliveryPackage pkg) {
        try{
            Deliverer bestDeliverer = null;
            double bestScore = Double.NEGATIVE_INFINITY;
            for (Deliverer deliverer : potentialDeliverers) {
                List<Affectation> affectations = affectationDAO.findByDelivererId(deliverer.getId());
                double score = calculateScore(deliverer, pkg , affectations);

                if (score > bestScore) {
                    bestScore = score;
                    bestDeliverer = deliverer;
                }
            }
            return bestDeliverer;
        }
        catch(Exception e){
            System.out.println("Error finding best deliverer: " + e.getMessage());
            return null;
        }
    }
   
    public Deliverer autoAssignPackage(int packageId) {
        try {
            DeliveryPackage pkg = packageDAO.findById(packageId);
            List<Deliverer> potentialDeliverers = delivererDAO.findAvailableByWeight(pkg.getWeight());
            
            Deliverer bestDeliverer = findBestDelivererForPackage(potentialDeliverers, pkg);

            if (bestDeliverer != null) {
                double newLoad = bestDeliverer.getCurrentLoad() + pkg.getWeight();
                bestDeliverer.setCurrentLoad(newLoad);
                if (newLoad >= bestDeliverer.getMaxWeight()) {
                    bestDeliverer.setAvailable(false);
                }
                Affectation aff = new Affectation(0, bestDeliverer.getId(), pkg.getIdPackage(), 
                                                AffectationStatus.PENDING, new Timestamp(System.currentTimeMillis()));
                
                pkg.setStatus(PackageStatus.ASSIGNED);
                
                affectationDAO.insert(aff);
                delivererDAO.update(bestDeliverer); 
                packageDAO.update(pkg);

                return bestDeliverer;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
