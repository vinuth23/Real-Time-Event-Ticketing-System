package com.ticketingsystem.model;

public class SystemStatus {
    private Integer totalTickets;
    private Integer availableTickets;  // Changed name for clarity
    private Integer maxTicketCapacity;
    private Integer ticketsPerRelease;
    private Integer numVendors;
    private Integer numCustomers;

    // Default constructor
    public SystemStatus() {
        this.totalTickets = 0;
        this.availableTickets = 0;
        this.maxTicketCapacity = 0;
        this.ticketsPerRelease = 0;
        this.numVendors = 0;
        this.numCustomers = 0;
    }

    // Constructor with parameters
    public SystemStatus(
            Integer totalTickets,
            Integer availableTickets,
            Integer maxTicketCapacity,
            Integer ticketsPerRelease,
            Integer numVendors,
            Integer numCustomers) {
        this.totalTickets = totalTickets;
        this.availableTickets = availableTickets;
        this.maxTicketCapacity = maxTicketCapacity;
        this.ticketsPerRelease = ticketsPerRelease;
        this.numVendors = numVendors;
        this.numCustomers = numCustomers;
    }

    // Getters and setters
    public Integer getTotalTickets() {
        return totalTickets;
    }

    public void setTotalTickets(Integer totalTickets) {
        this.totalTickets = totalTickets;
    }

    public Integer getTicketsPerRelease() {
        return ticketsPerRelease;
    }

    public void setTicketsPerRelease(Integer ticketsPerRelease) {
        this.ticketsPerRelease = ticketsPerRelease;
    }

    public Integer getMaxTicketCapacity() {
        return maxTicketCapacity;
    }

    public void setMaxTicketCapacity(Integer maxTicketCapacity) {
        this.maxTicketCapacity = maxTicketCapacity;
    }

    public Integer getAvailableTickets() {
        return availableTickets;
    }

    public void setAvailableTickets(Integer availableTickets) {
        this.availableTickets = availableTickets;
    }

    // New getters and setters for numVendors and numCustomers
    public Integer getNumVendors() {
        return numVendors;
    }

    public void setNumVendors(Integer numVendors) {
        this.numVendors = numVendors;
    }

    public Integer getNumCustomers() {
        return numCustomers;
    }

    public void setNumCustomers(Integer numCustomers) {
        this.numCustomers = numCustomers;
    }
}
