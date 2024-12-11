package com.coursework.ticketingsystem;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import java.util.logging.Level;

public class Customer implements Runnable {
    private static final Logger LOGGER = Logger.getLogger(Customer.class.getName());

    private final String name;
    private final TicketPool ticketPool;
    private final AtomicBoolean running;
    private final int retrievalRate;
    private final List<Ticket> purchasedTickets = new ArrayList<>();

    public Customer(String name, TicketPool ticketPool, AtomicBoolean running, int retrievalRate) {
        this.name = name;
        this.ticketPool = ticketPool;
        this.running = running;
        this.retrievalRate = retrievalRate;
    }

    @Override
    public void run() {
        try {
            LOGGER.info(name + " started purchasing tickets.");

            while (running.get()) {
                Ticket ticket = ticketPool.purchaseTicket();

                if (ticket != null) {
                    purchasedTickets.add(ticket);
                    LOGGER.info(String.format("%s purchased ticket %s. Total purchased: %d",
                            name, ticket.getTicketID(), purchasedTickets.size()));
                } else {
                    LOGGER.warning(name + " could not purchase a ticket. No tickets available.");
                }

                Thread.sleep(retrievalRate);
            }

            LOGGER.info(String.format("%s finished. Purchased %d tickets.", name, purchasedTickets.size()));
        } catch (InterruptedException e) {
            LOGGER.log(Level.WARNING, name + " was interrupted", e);
            Thread.currentThread().interrupt();
        }
    }

    public String getName() {
        return name;
    }

    public List<Ticket> getPurchasedTickets() {
        return new ArrayList<>(purchasedTickets);
    }
}