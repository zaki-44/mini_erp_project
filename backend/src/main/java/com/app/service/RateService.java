package com.app.service;

import com.app.dao.Implementation.RateDAO;
import com.app.model.Rate;
import java.sql.Date;
import java.util.List;


public class RateService {
    private RateDAO rateDAO = new RateDAO();
    public void rateDeliverer(int idDeliverer, int idClient, double rating, String comment) {
        Date currentDate = new Date(System.currentTimeMillis());
        Rate rate = new Rate(idDeliverer, idClient, rating, comment, currentDate);
        rateDAO.insert(rate);
    }
    public void updateRate(int idDeliverer, int idClient, double rating, String comment) {
        Date currentDate = new Date(System.currentTimeMillis());
        Rate rate = new Rate(idDeliverer, idClient, rating, comment, currentDate);
        rateDAO.update(rate);
    }
    public void deleteRate(int id) {
        rateDAO.delete(id);
    }
    public Rate getRateById(int id) {
        try {
            return rateDAO.findById(id);
        } catch (Exception e) {
            System.out.println("Error retrieving rate: " + e.getMessage());
            return null;
        }
    }
    public double calculateAverageRating(int idDeliverer) {
        try {
            List<Rate> rates = rateDAO.findAll();
            double total = 0.0;
            int count = 0;
            for (Rate rate : rates) {
                if (rate.getIdDeliverer() == idDeliverer) {
                    total += rate.getRating();
                    count++;
                }
            }
            return count == 0 ? 0.0 : total / count;
        } catch (Exception e) {
            System.out.println("Error calculating average rating: " + e.getMessage());
            return 0.0;
        }
    }
}
