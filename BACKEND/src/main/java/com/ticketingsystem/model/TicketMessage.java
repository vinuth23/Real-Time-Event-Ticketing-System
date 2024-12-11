package com.ticketingsystem.model;

import lombok.Data;

@Data
public class TicketMessage {
    private String status;
    private String ticketId;
    private int ticketCount;
}