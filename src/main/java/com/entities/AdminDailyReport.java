package com.entities;

import java.time.LocalDate;
import java.util.List;

public class AdminDailyReport {
    private User admin;
    private LocalDate date;
    private List<Payment> payments;
    private List<Reservation> reservations;
    private double totalAmount;

    public AdminDailyReport(User admin, LocalDate date, List<Payment> payments, List<Reservation> reservations) {
        this.admin = admin;
        this.date = date;
        this.payments = payments;
        this.reservations = reservations;
        this.totalAmount = payments.stream().mapToDouble(Payment::getMontantTotal).sum() +
                           reservations.stream().mapToDouble(Reservation::getTotalPrice).sum();
    }

    // Getters
    public User getAdmin() { return admin; }
    public LocalDate getDate() { return date; }
    public List<Payment> getPayments() { return payments; }
    public List<Reservation> getReservations() { return reservations; }
    public double getTotalAmount() { return totalAmount; }
    public int getTotalOperations() { return payments.size() + reservations.size(); }
}
