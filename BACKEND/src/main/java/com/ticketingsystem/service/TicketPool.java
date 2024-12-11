package com.ticketingsystem.service;

import lombok.Getter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class TicketPool {
    @Getter
    private final int maxTicketCapacity;
    private final BlockingQueue<String> ticketPool;
    private final AtomicInteger remainingTicketsToRelease;
    private final AtomicInteger ticketCounter = new AtomicInteger(1);
    private final ReentrantLock lock = new ReentrantLock();
    private AtomicInteger totalTickets;
    private volatile boolean vendorTestMode = true;

    @Getter private final int numVendors;
    @Getter private final int numCustomers;
    @Getter private final int ticketsPerRelease;

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

    public synchronized int getRemainingTickets() {
        return ticketPool.size();
    }

    public synchronized int getTotalTickets() {
        return totalTickets.get();
    }

    public synchronized int getRemainingToRelease() {
        return remainingTicketsToRelease.get();
    }

    public synchronized boolean hasMoreTicketsToRelease() {
        return remainingTicketsToRelease.get() > 0;
    }

    public synchronized boolean isPoolFull() {
        return ticketPool.size() >= maxTicketCapacity;
    }

    public synchronized void setVendorTestMode(boolean enabled) {
        this.vendorTestMode = enabled;
    }

    @Override
    public String toString() {
        return "TicketPool[available=" + ticketPool.size() +
                ", remainingToRelease=" + remainingTicketsToRelease.get() +
                ", total=" + totalTickets.get() + "]";
    }
}