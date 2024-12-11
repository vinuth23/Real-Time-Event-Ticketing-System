package com.ticketingsystem.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import com.ticketingsystem.model.ConfigurationUpdate;
import com.ticketingsystem.model.SystemStatus;
import com.ticketingsystem.model.TicketMessage;
import com.ticketingsystem.service.TicketingService;
import com.ticketingsystem.service.ConfigurationPersistenceService;
import org.springframework.context.event.EventListener;

//main WebSocket controller
@Controller
@Validated
public class TicketingWebSocketController {
    private static final Logger logger = LoggerFactory.getLogger(TicketingWebSocketController.class);
    private final TicketingService ticketingService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ConfigurationPersistenceService configService;

    //constructor for dependency injection
    public TicketingWebSocketController(TicketingService ticketingService,
                                        SimpMessagingTemplate messagingTemplate,
                                        ConfigurationPersistenceService configService) {
        this.ticketingService = ticketingService;
        this.messagingTemplate = messagingTemplate;
        this.configService = configService;
    }

    //start and update system configuration
    @MessageMapping("/system/start")
    @SendTo("/topic/system-status")
    public SystemStatus startSystem(@Valid @Validated(ConfigurationUpdate.VendorOperation.class) ConfigurationUpdate configuration) {
        logger.info("Received system start/update request with configuration: {}", configuration);
        return ticketingService.startSystem(configuration);
    }

    //start customer simulation process
    @MessageMapping("/customer/start")
    @SendTo("/topic/system-status")
    public SystemStatus startCustomerProcess(@Valid @Validated(ConfigurationUpdate.CustomerOperation.class) ConfigurationUpdate configuration) {
        logger.info("Received customer start request with configuration: {}", configuration);
        return ticketingService.startCustomerProcess(configuration);
    }

    //get current system state
    @MessageMapping("/system/state")
    @SendTo("/topic/system-status")
    public SystemStatus getSystemState() {
        return ticketingService.getCurrentState();
    }

    //save system configuration
    @MessageMapping("/settings/save")
    @SendTo("/topic/settings")
    public ConfigurationUpdate saveSettings(@Payload ConfigurationUpdate configuration) {
        logger.info("Saving system settings: {}", configuration);
        configService.saveConfiguration(configuration);
        ticketingService.updateConfiguration(configuration);
        return configuration;
    }

    //load saved configuration
    @MessageMapping("/settings/load")
    @SendTo("/topic/settings")
    public ConfigurationUpdate loadSettings() {
        logger.info("Loading system settings");
        return configService.loadConfiguration();
    }

    //process ticket release request from vendors
    @MessageMapping("/tickets/release")
    @SendTo("/topic/ticket-updates")
    public TicketMessage releaseTickets(@Valid TicketMessage message) {
        logger.info("Received ticket release request: {}", message);
        int ticketsAdded = ticketingService.processTicketRelease(message.getTicketCount());

        // Update operation status
        if (ticketsAdded > 0) {
            message.setStatus("SUCCESS");
            message.setTicketCount(ticketsAdded);
        } else {
            message.setStatus("FAILED");
        }

        logger.info("Processed ticket release: {}", message);
        return message;
    }

    //process ticket purchase request from customers
    @MessageMapping("/tickets/purchase")
    @SendTo("/topic/ticket-updates")
    public TicketMessage purchaseTickets(@Valid TicketMessage message) {
        logger.info("Received ticket purchase request: {}", message);
        String purchasedTicket = ticketingService.processTicketPurchase();

        // Update purchase status
        if (purchasedTicket != null) {
            message.setStatus("SUCCESS");
            message.setTicketId(purchasedTicket);
        } else {
            message.setStatus("FAILED");
        }

        logger.info("Processed ticket purchase: {}", message);
        return message;
    }

    //broadcast system status updates to all clients
    @EventListener
    public void handleTicketPoolUpdate(TicketingService.TicketPoolUpdateEvent event) {
        logger.info("Broadcasting ticket pool update: {}", event.getSystemStatus());
        messagingTemplate.convertAndSend("/topic/system-status", event.getSystemStatus());
    }
}