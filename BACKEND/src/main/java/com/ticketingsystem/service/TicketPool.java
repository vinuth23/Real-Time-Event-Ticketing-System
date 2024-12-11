package com.ticketingsystem.service;

import lombok.Getter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class TicketPool {
    @Getter
    private final int maxTicketCapacity;
    private final BlockingQueue<String> ticketPool;         // Thread-safe queue for tickets
    private final AtomicInteger remainingTicketsToRelease;  // Tracks unreleased tickets
    private final AtomicInteger ticketCounter = new AtomicInteger(1);
    private final ReentrantLock lock = new ReentrantLock(); // Lock for thread safety
    private AtomicInteger totalTickets;                     // Total tickets in system
    private volatile boolean vendorTestMode = true;

    @Getter private final int numVendors;
    @Getter private final int numCustomers;
    @Getter private final int ticketsPerRelease;

    //initialize ticket pool with capacity and constraints
    public TicketPool(int initialTotalTickets, int maxTicketCapacity, int numVendors,
                      int numCustomers, int ticketsPerRelease) {
        this.maxTicketCapacity = maxTicketCapacity;
        this.ticketPool = new LinkedBlockingQueue<>(maxTicketCapacity);
        this.remainingTicketsToRelease = new AtomicInteger(initialTotalTickets);
        this.totalTickets = new AtomicInteger(initialTotalTickets);
        this.numVendors = numVendors;
        this.numCustomers = numCustomers;
        this.ticketsPerRelease = ticketsPerRelease;

        System.out.println("TicketPool initialized: Total=" + initialTotalTickets +
                ", MaxCapacity=" + maxTicketCapacity +
                ", TicketsPerRelease=" + ticketsPerRelease);
    }

    //add new tickets to the total pool
    public synchronized void addNewTickets(int additionalTickets) {
        lock.lock();
        try {
            int newTotal = totalTickets.get() + additionalTickets;
            totalTickets.set(newTotal);
            remainingTicketsToRelease.addAndGet(additionalTickets);
            System.out.println("Added " + additionalTickets + " new tickets. New total: " + newTotal +
                    ", Remaining to release: " + remainingTicketsToRelease.get());
        } finally {
            lock.unlock();
        }
    }

    //release tickets to the available pool
    public synchronized int addTickets(int ticketsToAdd) {
        lock.lock();
        try {
            if (remainingTicketsToRelease.get() <= 0) {
                System.out.println("No more tickets to release");
                return 0;
            }

            int actualToAdd = Math.min(ticketsToAdd, remainingTicketsToRelease.get());
            int added = 0;

            for (int i = 0; i < actualToAdd; i++) {
                String ticket = "Ticket" + ticketCounter.getAndIncrement();
                if (ticketPool.offer(ticket)) {
                    added++;
                    remainingTicketsToRelease.decrementAndGet();
                    System.out.println("Added ticket: " + ticket +
                            ". Pool size now: " + ticketPool.size() +
                            ", Remaining to release: " + remainingTicketsToRelease.get());
                }
            }

            return added;
        } finally {
            lock.unlock();
        }
    }

    //purchase a ticket from the available pool
    public synchronized String purchaseTicket() {
        lock.lock();
        try {
            if (ticketPool.isEmpty()) {
                System.out.println("No tickets available for purchase");
                return null;
            }
            String ticket = ticketPool.poll();
            System.out.println("Ticket purchased: " + ticket +
                    ". Remaining in pool: " + ticketPool.size());
            return ticket;
        } finally {
            lock.unlock();
        }
    }

    //get number of tickets currently in pool
    public synchronized int getRemainingTickets() {
        return ticketPool.size();
    }

    //get total number of tickets in system
    public synchronized int getTotalTickets() {
        return totalTickets.get();
    }

    //get number of tickets not yet released
    public synchronized int getRemainingToRelease() {
        return remainingTicketsToRelease.get();
    }

    //check if there are more tickets to release
    public synchronized boolean hasMoreTicketsToRelease() {
        return remainingTicketsToRelease.get() > 0;
    }

    //check if pool is at capacity
    public synchronized boolean isPoolFull() {
        return ticketPool.size() >= maxTicketCapacity;
    }

    //set vendor test mode status
    public synchronized void setVendorTestMode(boolean enabled) {
        this.vendorTestMode = enabled;
    }

    //string representation of pool state
    @Override
    public String toString() {
        return "TicketPool[available=" + ticketPool.size() +
                ", remainingToRelease=" + remainingTicketsToRelease.get() +
                ", total=" + totalTickets.get() + "]";
    }
}