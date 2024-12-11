package com.ticketingsystem.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketingsystem.model.ConfigurationUpdate;
import com.ticketingsystem.model.SystemStatus;
import com.ticketingsystem.model.TicketMessage;
import com.ticketingsystem.service.TicketingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TicketingWebSocketIntegrationTest {

    @LocalServerPort
    private Integer port;

    @Autowired
    private TicketingService ticketingService;

    private WebSocketStompClient stompClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AtomicReference<StompSession> sessionRef = new AtomicReference<>();

    @BeforeEach
    void setup() throws ExecutionException, InterruptedException, TimeoutException {
        List<Transport> transports = new ArrayList<>();
        transports.add(new WebSocketTransport(new StandardWebSocketClient()));
        SockJsClient sockJsClient = new SockJsClient(transports);

        this.stompClient = new WebSocketStompClient(sockJsClient);
        this.stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        // Connect once for all tests
        StompSession session = stompClient
                .connect(String.format("ws://localhost:%d/ws", port), new StompSessionHandlerAdapter() {})
                .get(5, TimeUnit.SECONDS);
        sessionRef.set(session);

        // Initialize system for all tests
        initializeTestSystem();
        Thread.sleep(1000); // Wait for initialization
    }

    @Test
    void testTicketPurchase() throws ExecutionException, InterruptedException, TimeoutException {
        CompletableFuture<TicketMessage> completableFuture = new CompletableFuture<>();
        StompSession session = sessionRef.get();

        // Initialize system before testing ticket purchase
        initializeTestSystem();
        Thread.sleep(1000); // Wait for initialization

        // Subscribe to updates
        session.subscribe("/topic/ticket-updates", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return TicketMessage.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                TicketMessage msg = (TicketMessage) payload;
                if (msg.getStatus() != null && msg.getTicketId() != null) {
                    completableFuture.complete(msg);
                }
            }
        });

        Thread.sleep(1000); // Wait for subscription to be active

        // Create and send purchase message
        TicketMessage purchaseMessage = new TicketMessage();
        purchaseMessage.setTicketCount(1);
        session.send("/app/tickets/purchase", purchaseMessage);

        // Wait for response
        TicketMessage response = completableFuture.get(5, TimeUnit.SECONDS);

        assertNotNull(response, "Response should not be null");
        assertNotNull(response.getTicketId(), "Ticket ID should not be null");
        assertEquals("SUCCESS", response.getStatus(), "Purchase should be successful");
    }

    @Test
    void testRealTimeUpdates() throws ExecutionException, InterruptedException, TimeoutException, JsonProcessingException {
        CompletableFuture<SystemStatus> completableFuture = new CompletableFuture<>();
        StompSession session = sessionRef.get();

        // Subscribe first
        session.subscribe("/topic/system-status", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return SystemStatus.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                SystemStatus status = (SystemStatus) payload;
                if (status != null && status.getTotalTickets() != null) {
                    completableFuture.complete(status);
                }
            }
        });

        Thread.sleep(1000); // Wait for subscription to be active

        // Send a system initialization message
        ConfigurationUpdate config = new ConfigurationUpdate();
        config.setTotalTickets(10);
        config.setTicketsPerRelease(2);
        config.setMaxTicketCapacity(5);
        config.setNumVendors(1);
        config.setNumCustomers(1);
        config.setReleaseInterval(1000);

        String payload = objectMapper.writeValueAsString(config);
        session.send("/app/system/initialize", payload);

        // Wait for status update with increased timeout
        SystemStatus status = completableFuture.get(5, TimeUnit.SECONDS);

        assertNotNull(status, "Status should not be null");
        assertNotNull(status.getAvailableTickets(), "Available tickets should not be null");
        assertTrue(status.getAvailableTickets() <= status.getMaxTicketCapacity(),
                "Available tickets should not exceed capacity");
    }

    @Test
    void testConfigurationUpdate() throws ExecutionException, InterruptedException, TimeoutException {
        CompletableFuture<ConfigurationUpdate> completableFuture = new CompletableFuture<>();
        StompSession session = sessionRef.get();

        session.subscribe("/topic/configuration-updates", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return ConfigurationUpdate.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                completableFuture.complete((ConfigurationUpdate) payload);
            }
        });

        // Send configuration update
        ConfigurationUpdate update = new ConfigurationUpdate();
        update.setTotalTickets(20);
        update.setTicketsPerRelease(3);
        update.setMaxTicketCapacity(10);
        update.setNumVendors(2);
        update.setNumCustomers(2);
        update.setReleaseInterval(1000);

        session.send("/app/configuration/update", update);

        // Wait for response with increased timeout
        ConfigurationUpdate response = completableFuture.get(15, TimeUnit.SECONDS);

        assertNotNull(response, "Response should not be null");
        assertEquals(20, response.getTotalTickets(), "Total tickets should match");
        assertEquals(3, response.getTicketsPerRelease(), "Tickets per release should match");
        assertEquals(10, response.getMaxTicketCapacity(), "Max capacity should match");
    }

    private void initializeTestSystem() {
        ConfigurationUpdate config = new ConfigurationUpdate();
        config.setTotalTickets(10);
        config.setTicketsPerRelease(2);
        config.setMaxTicketCapacity(5);
        config.setNumVendors(1);
        config.setNumCustomers(1);
        config.setReleaseInterval(1000);

        ticketingService.initializeSystem(
                config.getTotalTickets(),
                config.getTicketsPerRelease(),
                config.getMaxTicketCapacity(),
                config.getNumVendors(),
                config.getNumCustomers()
        );
    }
}