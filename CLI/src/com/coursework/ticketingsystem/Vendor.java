package com.coursework.ticketingsystem;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import java.util.logging.Level;

public class Vendor implements Runnable {
    private static final Logger LOGGER = Logger.getLogger(Vendor.class.getName());

    private final String name;
    private final TicketPool ticketPool;
    private final int ticketsPerRelease;
    private final AtomicBoolean running;
    private final int releaseInterval;

    public Vendor(String name, TicketPool ticketPool, int ticketsPerRelease,
                  AtomicBoolean running, int releaseInterval) {
        this.name = name;
        this.ticketPool = ticketPool;
        this.ticketsPerRelease = ticketsPerRelease;
        this.running = running;
        this.releaseInterval = releaseInterval;
    }

    @Override
    public void run() {
        try {
            LOGGER.info(name + " started releasing tickets.");

            while (running.get()) {
                if (ticketPool.getRemainingTickets() < ticketPool.getMaxTicketCapacity()) {
                    int ticketsAdded = ticketPool.addTickets(ticketsPerRelease);
                    if (ticketsAdded > 0) {
                        LOGGER.info(String.format("%s added %d tickets. Current pool size: %d",
                                name, ticketsAdded, ticketPool.getRemainingTickets()));
                    }
                } else {
                    LOGGER.info(String.format("%s waiting - Ticket pool at maximum capacity (%d/%d)",
                            name, ticketPool.getRemainingTickets(), ticketPool.getMaxTicketCapacity()));
                }
                Thread.sleep(releaseInterval);
            }

            LOGGER.info(name + " has finished releasing tickets.");
        } catch (InterruptedException e) {
            LOGGER.log(Level.WARNING, name + " was interrupted", e);
            Thread.currentThread().interrupt();
        }
    }

    public String getName() {
        return name;
    }
}