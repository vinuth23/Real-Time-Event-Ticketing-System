package com.ticketingsystem.service;

import com.ticketingsystem.model.ConfigurationUpdate;

public class Configuration {
    private int totalTickets;
    private int releaseInterval;
    private int purchaseInterval;
    private int ticketsPerRelease;
    private int maxTicketCapacity;

    public Configuration(int totalTickets, int releaseInterval, int purchaseInterval, int ticketsPerRelease, int maxTicketCapacity) {
        this.totalTickets = totalTickets;
        this.releaseInterval = releaseInterval;
        this.purchaseInterval = purchaseInterval;
        this.ticketsPerRelease = ticketsPerRelease;
        this.maxTicketCapacity = maxTicketCapacity;
        validateConfig();
    }

    public int getTotalTickets() {
        return totalTickets;
    }

    public int getReleaseInterval() {
        return releaseInterval;
    }

    public int getPurchaseInterval() {
        return purchaseInterval;
    }

    public int getTicketsPerRelease() {
        return ticketsPerRelease;
    }

    public int getMaxTicketCapacity() {
        return maxTicketCapacity;
    }

    public void validateConfig() {
        StringBuilder errors = new StringBuilder();

        if (totalTickets <= 0) {
            errors.append("Total tickets must be greater than zero. ");
        }
        if (releaseInterval <= 0) {
            errors.append("Release interval must be greater than zero seconds. ");
        }
        if (purchaseInterval <= 0) {
            errors.append("Purchase interval must be greater than zero seconds. ");
        }
        if (ticketsPerRelease <= 0) {
            errors.append("Tickets per release must be greater than zero. ");
        }
        if (maxTicketCapacity <= 0) {
            errors.append("Maximum ticket capacity must be greater than zero. ");
        }
        if (maxTicketCapacity < ticketsPerRelease) {
            errors.append("Maximum capacity cannot be less than tickets per release. ");
        }
        if (totalTickets > maxTicketCapacity) {
            errors.append("Total tickets cannot exceed maximum capacity. ");
        }

        if (errors.length() > 0) {
            throw new IllegalArgumentException(errors.toString().trim());
        }
    }

    public void updateConfiguration(ConfigurationUpdate configUpdate) {
        int originalTotal = this.totalTickets;
        int originalReleaseInterval = this.releaseInterval;
        int originalPurchaseInterval = this.purchaseInterval;
        int originalPerRelease = this.ticketsPerRelease;
        int originalCapacity = this.maxTicketCapacity;

        try {
            if (configUpdate.getTotalTickets() != null) {
                this.totalTickets = configUpdate.getTotalTickets();
            }
            if (configUpdate.getReleaseInterval() != null) {
                this.releaseInterval = configUpdate.getReleaseInterval();
            }
            if (configUpdate.getPurchaseInterval() != null) {
                this.purchaseInterval = configUpdate.getPurchaseInterval();
            }
            if (configUpdate.getTicketsPerRelease() != null) {
                this.ticketsPerRelease = configUpdate.getTicketsPerRelease();
            }
            if (configUpdate.getMaxTicketCapacity() != null) {
                this.maxTicketCapacity = configUpdate.getMaxTicketCapacity();
            }

            validateConfig();
        } catch (IllegalArgumentException e) {
            this.totalTickets = originalTotal;
            this.releaseInterval = originalReleaseInterval;
            this.purchaseInterval = originalPurchaseInterval;
            this.ticketsPerRelease = originalPerRelease;
            this.maxTicketCapacity = originalCapacity;
            throw e;
        }
    }

    @Override
    public String toString() {
        return String.format(
                "Configuration(totalTickets=%d, releaseInterval=%ds, purchaseInterval=%ds, ticketsPerRelease=%d, maxCapacity=%d)",
                totalTickets, releaseInterval, purchaseInterval, ticketsPerRelease, maxTicketCapacity
        );
    }
}