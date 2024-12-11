package com.coursework.ticketingsystem;

public class Ticket {
    private final String ticketID;

    public Ticket(String ticketID) {
        this.ticketID = ticketID;
    }

    public String getTicketID() {
        return ticketID;
    }

    @Override
    public String toString() {
        return "Ticket{id='" + ticketID + "'}";
    }
}