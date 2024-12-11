package com.coursework.ticketingsystem;


import java.util.logging.Logger;
import java.util.logging.Level;

public class Configuration {
    private static final Logger LOGGER = Logger.getLogger(Configuration.class.getName());

    private int totalTickets;
    private int releaseInterval;
    private int ticketsPerRelease;
    private int maxTicketCapacity;
    private int customerRetrievalRate;

    public Configuration(int totalTickets, int releaseInterval, int ticketsPerRelease,
                         int maxTicketCapacity, int customerRetrievalRate) {
        this.totalTickets = totalTickets;
        this.releaseInterval = releaseInterval;
        this.ticketsPerRelease = ticketsPerRelease;
        this.maxTicketCapacity = maxTicketCapacity;
        this.customerRetrievalRate = customerRetrievalRate;

        validateConfig();
    }

    public int getTotalTickets() {
        return totalTickets;
    }

    public int getReleaseInterval() {
        return releaseInterval;
    }

    public int getTicketsPerRelease() {
        return ticketsPerRelease;
    }

    public int getMaxTicketCapacity() {
        return maxTicketCapacity;
    }

    public int getCustomerRetrievalRate() {
        return customerRetrievalRate;
    }

    public void validateConfig() {
        try {
            if (totalTickets <= 0) {
                throw new IllegalArgumentException("Total tickets must be greater than zero.");
            }
            if (releaseInterval <= 0) {
                throw new IllegalArgumentException("Release interval must be greater than zero.");
            }
            if (ticketsPerRelease <= 0) {
                throw new IllegalArgumentException("Tickets per release must be greater than zero.");
            }
            if (maxTicketCapacity <= 0) {
                throw new IllegalArgumentException("Max ticket capacity must be greater than zero.");
            }
            if (customerRetrievalRate <= 0) {
                throw new IllegalArgumentException("Customer retrieval rate must be greater than zero.");
            }

            // Additional validation checks
            if (totalTickets > maxTicketCapacity) {
                throw new IllegalArgumentException("Total tickets cannot exceed max ticket capacity.");
            }

            LOGGER.info("Configuration validated successfully.");
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.SEVERE, "Configuration validation failed", e);
            throw e;
        }
    }
}