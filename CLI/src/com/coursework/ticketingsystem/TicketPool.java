package com.coursework.ticketingsystem;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.logging.Level;

public class TicketPool {
    private static final Logger LOGGER = Logger.getLogger(TicketPool.class.getName());

    private final int maxTicketCapacity;
    private final ConcurrentLinkedQueue<Ticket> ticketPool = new ConcurrentLinkedQueue<>();
    private final AtomicInteger totalTicketsCreated = new AtomicInteger(0);

    public TicketPool(int totalTickets, int maxTicketCapacity) {
        this.maxTicketCapacity = maxTicketCapacity;

        // Initially, add tickets to the pool
        for (int i = 0; i < Math.min(totalTickets, maxTicketCapacity); i++) {
            addInitialTicket();
        }

        LOGGER.info(String.format("TicketPool initialized with %d initial tickets", ticketPool.size()));
    }

    private void addInitialTicket() {
        Ticket ticket = new Ticket("Ticket-" + (totalTicketsCreated.incrementAndGet()));
        ticketPool.add(ticket);
    }

    public synchronized int addTickets(int ticketsPerRelease) {
        int ticketsAdded = 0;

        try {
            // Ensure we don't exceed max capacity
            int availableSpace = maxTicketCapacity - ticketPool.size();
            int ticketsToAdd = Math.min(ticketsPerRelease, availableSpace);

            for (int i = 0; i < ticketsToAdd; i++) {
                Ticket newTicket = new Ticket("Ticket-" + (totalTicketsCreated.incrementAndGet()));
                ticketPool.add(newTicket);
                ticketsAdded++;
            }

            if (ticketsAdded > 0) {
                LOGGER.info(String.format("%d tickets added to pool. Current pool size: %d",
                        ticketsAdded, ticketPool.size()));
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error adding tickets to pool", e);
        }

        return ticketsAdded;
    }

    public synchronized Ticket purchaseTicket() {
        if (ticketPool.isEmpty()) {
            LOGGER.warning("Attempt to purchase ticket from empty pool");
            return null;
        }

        Ticket ticket = ticketPool.poll();

        if (ticket != null) {
            LOGGER.info(String.format("Ticket %s purchased. Remaining tickets: %d",
                    ticket.getTicketID(), ticketPool.size()));
        }

        return ticket;
    }

    public synchronized int getRemainingTickets() {
        return ticketPool.size();
    }

    public int getMaxTicketCapacity() {
        return maxTicketCapacity;
    }
}