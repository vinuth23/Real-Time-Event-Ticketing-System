package com.ticketingsystem.service;

import org.springframework.context.ApplicationEventPublisher;
import com.ticketingsystem.model.SystemStatus;

public class Customer implements Runnable {
    private final String name;
    private final TicketPool ticketPool;
    private final ApplicationEventPublisher eventPublisher;
    private volatile boolean running = true;
    private final int targetTickets;
    private final int purchaseInterval;
    private int purchasedTickets = 0;

    public Customer(String name, TicketPool ticketPool, ApplicationEventPublisher eventPublisher,
                    int targetTickets, int purchaseInterval) {
        this.name = name;
        this.ticketPool = ticketPool;
        this.eventPublisher = eventPublisher;
        this.targetTickets = targetTickets;
        this.purchaseInterval = purchaseInterval;
    }

    @Override
    public void run() {
        try {
            // Check if enough tickets are available before starting
            System.out.println(name + " starting with purchase interval: " + purchaseInterval + " seconds");
            if (ticketPool.getRemainingTickets() < targetTickets) {
                System.out.println(name + " cannot start purchasing - Requested " + targetTickets +
                        " tickets but only " + ticketPool.getRemainingTickets() + " available");
                return;
            }

            System.out.println(name + " started attempting to purchase " + targetTickets + " tickets");

            while (running && purchasedTickets < targetTickets && !Thread.currentThread().isInterrupted()) {
                String ticket = ticketPool.purchaseTicket();

                if (ticket != null) {
                    purchasedTickets++;
                    System.out.println(name + " purchased ticket " + purchasedTickets +
                            "/" + targetTickets + ": " + ticket);
                    publishStatus();

                    if (purchasedTickets < targetTickets) {
                        Thread.sleep(purchaseInterval * 1000L);
                    }
                } else {
                    System.out.println(name + " waiting for available tickets...");
                    Thread.sleep(1000);
                }
            }

            System.out.println(name + " finished purchasing tickets. Total purchased: " + purchasedTickets);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println(name + " was interrupted.");
        }
    }

    private void publishStatus() {
        SystemStatus status = new SystemStatus(
                ticketPool.getTotalTickets(),
                ticketPool.getRemainingTickets(),
                ticketPool.getMaxTicketCapacity(),
                ticketPool.getTicketsPerRelease(),
                ticketPool.getNumVendors(),
                ticketPool.getNumCustomers()
        );
        eventPublisher.publishEvent(new TicketingService.TicketPoolUpdateEvent(this, status));
    }

    public void stop() {
        running = false;
    }
}