package com.ticketingsystem.service;

import java.util.concurrent.CountDownLatch;
import org.springframework.context.ApplicationEventPublisher;
import com.ticketingsystem.model.SystemStatus;

public class Vendor implements Runnable {
    private final String name;
    private final TicketPool ticketPool;
    private final CountDownLatch latch;
    private final ApplicationEventPublisher eventPublisher;
    private final int releaseIntervalSeconds;
    private volatile boolean running = true;
    private int ticketsReleased = 0;

    public Vendor(String name, TicketPool ticketPool, int ticketsPerRelease,
                  CountDownLatch latch, ApplicationEventPublisher eventPublisher,
                  int releaseIntervalSeconds) {
        this.name = name;
        this.ticketPool = ticketPool;
        this.latch = latch;
        this.eventPublisher = eventPublisher;
        this.releaseIntervalSeconds = releaseIntervalSeconds;

        System.out.println(name + " created - Release interval: " + releaseIntervalSeconds +
                "s, Total tickets to release: " + ticketPool.getTotalTickets());
    }

    @Override
    public void run() {
        try {
            System.out.println(name + " starting ticket release process");

            while (running && ticketsReleased < ticketPool.getTotalTickets() &&
                    !Thread.currentThread().isInterrupted()) {

                System.out.println(name + " attempting to release ticket " + (ticketsReleased + 1));
                int released = ticketPool.addTickets(1);

                if (released > 0) {
                    ticketsReleased++;
                    System.out.println(name + " released ticket " + ticketsReleased + "/" +
                            ticketPool.getTotalTickets());
                    publishStatus();

                    if (ticketsReleased < ticketPool.getTotalTickets()) {
                        System.out.println(name + " waiting " + releaseIntervalSeconds +
                                " seconds before next release");
                        Thread.sleep(releaseIntervalSeconds * 1000L);
                    }
                } else {
                    System.out.println(name + " failed to release ticket");
                    break;
                }
            }

            System.out.println(name + " finished. Released " + ticketsReleased + " tickets.");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println(name + " was interrupted");
        } finally {
            latch.countDown();
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