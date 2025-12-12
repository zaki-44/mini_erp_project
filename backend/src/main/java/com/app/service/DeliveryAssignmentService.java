package com.app.service;

import java.util.Arrays;
import java.util.HashMap;
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
import com.app.model.Deliverer;

public class DeliveryAssignmentService {
    private DelivererDAO delivererDAO;
    private DeliveryPackageDAO packageDAO;
    private AffectationDAO affectationDAO;
    
    private static final double VEHICLE_MATCH_WEIGHT = 40.0;
    private static final double WORKLOAD_PENALTY_PER_ASSIGNMENT = 10.0;
    private static final double PERFORMANCE_BONUS_MAX = 20.0;
    private static final double RESPONSE_BONUS_MAX = 10.0;
    private static final double AREA_MATCH_BONUS = 5.0;
    
    // Vehicle compatibility matrix
    private static final Map<VehicleType, List<String>> VEHICLE_COMPATIBILITY = new HashMap<>();
    static {
        VEHICLE_COMPATIBILITY.put(VehicleType.BIKE, Arrays.asList("BIKE"));
        VEHICLE_COMPATIBILITY.put(VehicleType.CAR, Arrays.asList("CAR", "BIKE"));
        VEHICLE_COMPATIBILITY.put(VehicleType.TRUCK, Arrays.asList("TRUCK", "CAR", "BIKE"));
    }


    public DeliveryAssignmentService() {
        this.delivererDAO = new DelivererDAO();
        this.packageDAO = new DeliveryPackageDAO();
        this.affectationDAO = new AffectationDAO();
    }
    
    private boolean isVehicleUpgrade(String delivererVehicle, VehicleType neededVehicle) {
        if (neededVehicle == VehicleType.BIKE) {
            return delivererVehicle.equals("CAR");
        } else if (neededVehicle == VehicleType.CAR) {
            return delivererVehicle.equals("TRUCK");
        }
        return false;
    }
    private double calculateVehicleScore(Deliverer deliverer, DeliveryPackage pkg) {
        if (pkg.getVehicleTypeNeeded() == null) {
            return 20.0; // No specific vehicle requirement - neutral
        }
        
        String delivererVehicle = deliverer.getVehicleType();
        VehicleType neededVehicle = pkg.getVehicleTypeNeeded();
        
        // Perfect match
        if (delivererVehicle.equals(neededVehicle.name())) {
            return VEHICLE_MATCH_WEIGHT; // Full points
        }

        

        // Check compatibility (can deliverer's vehicle handle the package?)
        if (VEHICLE_COMPATIBILITY.get(neededVehicle).contains(delivererVehicle)) {
            // Compatible but not perfect
            if (isVehicleUpgrade(delivererVehicle, neededVehicle)) {
                // Upgrade (e.g., TRUCK for CAR package) - less efficient but works
                return VEHICLE_MATCH_WEIGHT * 0.75; // 75% of full points
            }
            return VEHICLE_MATCH_WEIGHT * 0.5; // 50% for basic compatibility | Truck for Bike
        }
        
        return -20.0; // Incompatible vehicle - heavy penalty
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
    private double calculateWorkloadPenalty(Deliverer deliverer) throws Exception {
        List<Affectation> allAffectations = affectationDAO.findAll();
        int activeAssignments = 0;
        
        for (Affectation aff : allAffectations) {
            if (aff.getIdDeliverer() == deliverer.getId() && 
                (aff.getStatus() == AffectationStatus.PENDING || 
                 aff.getStatus() == AffectationStatus.ACCEPTED)) {
                activeAssignments++;
            }
        }
        
        return activeAssignments * WORKLOAD_PENALTY_PER_ASSIGNMENT;
    }
    
    
    public double calculateScore(Deliverer deliverer, DeliveryPackage pkg) {
        try {
            double score = 100.0; // Base score
            
            // 1. Vehicle Type Match (CRITICAL - up to 40 points)
            score += calculateVehicleScore(deliverer, pkg);
            
            // 2. Current Workload Penalty (IMPORTANT - negative)
            score -= calculateWorkloadPenalty(deliverer);
            
            // 3. Performance History (IMPORTANT - up to 20 points)
            score += calculatePerformanceScore(deliverer);
            
            // 4. Area Match Bonus (SMART ROUTING - up to 5 points)
            // Must be in the same Area 
            
            // 5. Package Weight Match (EFFICIENCY - up to 10 points)
            score += calculateWeightCapacityScore(deliverer, pkg);
            
            // 6. Experience Bonus (NEW DELIVERER SUPPORT)
            score += calculateExperienceBonus(deliverer);
            
            return Math.max(0, score); // Never return negative score
            
        } catch (Exception e) {
            System.out.println("Error calculating score: " + e.getMessage());
            return 0.0; // Return minimum score on error
        }
    }
    


    public Deliverer findBestDelivererForPackage(DeliveryPackage pkg) {
        try{
            Deliverer bestDeliverer = null;
            double bestScore = Double.NEGATIVE_INFINITY;
            List<Deliverer> availableDeliverers = delivererDAO.findAllAvailable();
            for (Deliverer deliverer : availableDeliverers) {
                double score = calculateScore(deliverer, pkg);
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
   
    public void autoAssignPackage(int packageId) {
        try{
            DeliveryPackage pkg = packageDAO.findById(packageId);
            if (pkg == null) {
                throw new IllegalArgumentException("Package not found with ID: " + packageId);
            }
            Deliverer bestDeliverer = findBestDelivererForPackage(pkg);
            if (bestDeliverer != null) {
                Affectation aff = new Affectation();
                aff.setIdDeliverer(bestDeliverer.getId());
                aff.setIdPackage(pkg.getIdPackage());
                aff.setStatus(AffectationStatus.PENDING);
                aff.setAssignedAt(new java.sql.Timestamp(System.currentTimeMillis()));
                pkg.setStatus(PackageStatus.ASSIGNED);
                affectationDAO.insert(aff);
            } else {
                throw new IllegalStateException("No available deliverers found for package ID: " + packageId);
            }
        }
        catch(Exception e){
            System.out.println("Error during auto-assigning package: " + e.getMessage());
        }
    }

}
