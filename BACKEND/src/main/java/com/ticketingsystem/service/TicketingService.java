package com.ticketingsystem.service;

import com.ticketingsystem.model.ConfigurationUpdate;
import com.ticketingsystem.model.SystemStatus;
import lombok.Getter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

//main service for managing ticket system operations
@Service
public class TicketingService {
    private TicketPool ticketPool;
    private ExecutorService executorService;
    private Configuration configuration;
    private final ApplicationEventPublisher eventPublisher;
    private final ConfigurationPersistenceService configService;
    private final List<Vendor> vendors = new ArrayList<>();
    private final List<Customer> customers = new ArrayList<>();
    private volatile boolean isSystemRunning = false;

    //initialize service with event publisher and config service
    public TicketingService(ApplicationEventPublisher eventPublisher,
                            ConfigurationPersistenceService configService) {
        this.eventPublisher = eventPublisher;
        this.configService = configService;
    }

    //start a new customer process with given configuration
    public synchronized SystemStatus startCustomerProcess(ConfigurationUpdate configUpdate) {
        if (!isSystemRunning) {
            throw new IllegalStateException("System must be running before starting customer process");
        }

        try {
            System.out.println("Starting customer with purchase interval: " + configUpdate.getPurchaseInterval());

            Customer customer = new Customer(
                    "Customer" + (customers.size() + 1),
                    ticketPool,
                    eventPublisher,
                    configUpdate.getTotalTickets(),
                    configUpdate.getPurchaseInterval()
            );

            customers.add(customer);
            executorService.submit(customer);

            SystemStatus status = getSystemStatus(vendors.size(), customers.size());
            eventPublisher.publishEvent(new TicketPoolUpdateEvent(this, status));
            return status;
        } catch (Exception e) {
            System.err.println("Error starting customer: " + e.getMessage());
            throw new IllegalArgumentException("Failed to start customer", e);
        }
    }

    //add more tickets to the existing pool
    public synchronized SystemStatus addMoreTickets(ConfigurationUpdate configUpdate) {
        if (!isSystemRunning || ticketPool == null) {
            return startSystem(configUpdate);
        }

        ticketPool.addNewTickets(configUpdate.getTotalTickets());

        configuration = new Configuration(
                ticketPool.getTotalTickets(),
                configUpdate.getReleaseInterval(),
                configUpdate.getPurchaseInterval(),
                configUpdate.getTicketsPerRelease(),
                configUpdate.getMaxTicketCapacity()
        );

        if (!vendors.isEmpty()) {
            CountDownLatch vendorLatch = new CountDownLatch(1);
            Vendor vendor = new Vendor(
                    "Vendor" + (vendors.size() + 1),
                    ticketPool,
                    configuration.getTicketsPerRelease(),
                    vendorLatch,
                    eventPublisher,
                    configuration.getReleaseInterval()
            );
            vendors.add(vendor);
            executorService.submit(vendor);
        }

        SystemStatus status = getSystemStatus(vendors.size(), customers.size());
        eventPublisher.publishEvent(new TicketPoolUpdateEvent(this, status));
        return status;
    }

    //initialize or restart the ticket system
    public SystemStatus startSystem(ConfigurationUpdate configUpdate) {
        if (isSystemRunning && ticketPool != null) {
            return addMoreTickets(configUpdate);
        }

        System.out.println("Starting system with release interval: " + configUpdate.getReleaseInterval() +
                " seconds, purchase interval: " + configUpdate.getPurchaseInterval() + " seconds");

        this.configuration = new Configuration(
                configUpdate.getTotalTickets(),
                configUpdate.getReleaseInterval(),
                configUpdate.getPurchaseInterval(),
                configUpdate.getTicketsPerRelease(),
                configUpdate.getMaxTicketCapacity()
        );

        return initializeSystem(
                configuration.getTotalTickets(),
                configuration.getTicketsPerRelease(),
                configuration.getMaxTicketCapacity(),
                configUpdate.getNumVendors(),
                configUpdate.getNumCustomers()
        );
    }

    //update system configuration with new settings
    public SystemStatus updateConfiguration(ConfigurationUpdate configUpdate) {
        if (!isValidConfiguration(configUpdate)) {
            throw new IllegalArgumentException("Invalid configuration values");
        }

        configuration = new Configuration(
                configUpdate.getTotalTickets(),
                configUpdate.getReleaseInterval(),
                configUpdate.getPurchaseInterval(),
                configUpdate.getTicketsPerRelease(),
                configUpdate.getMaxTicketCapacity()
        );

        configService.saveConfiguration(configUpdate);

        if (ticketPool != null) {
            SystemStatus status = getSystemStatus(vendors.size(), customers.size());
            eventPublisher.publishEvent(new TicketPoolUpdateEvent(this, status));
            return status;
        }

        return new SystemStatus();
    }

    //validate configuration parameters
    private boolean isValidConfiguration(ConfigurationUpdate configUpdate) {
        if (configUpdate.getTotalTickets() <= 0) {
            System.err.println("Total tickets must be greater than 0.");
            return false;
        }

        if (configUpdate.getReleaseInterval() <= 0) {
            System.err.println("Release interval must be greater than 0.");
            return false;
        }

        if (configUpdate.getPurchaseInterval() <= 0) {
            System.err.println("Purchase interval must be greater than 0.");
            return false;
        }

        if (configUpdate.getTicketsPerRelease() <= 0 || configUpdate.getTicketsPerRelease() > configUpdate.getTotalTickets()) {
            System.err.println("Tickets per release must be greater than 0 and less than or equal to total tickets.");
            return false;
        }

        if (configUpdate.getMaxTicketCapacity() < configUpdate.getTotalTickets()) {
            System.err.println("Max ticket capacity must be greater than or equal to total tickets.");
            return false;
        }

        return true;
    }

    //initialize system with given parameters
    public synchronized SystemStatus initializeSystem(int totalTickets, int ticketsPerRelease,
                                                      int maxTicketCapacity, int numVendors, int numCustomers) {
        if (isSystemRunning) {
            shutdownSystem();
        }

        try {
            Configuration tempConfig = new Configuration(
                    totalTickets,
                    configuration.getReleaseInterval(),
                    configuration.getPurchaseInterval(),
                    ticketsPerRelease,
                    maxTicketCapacity
            );
            tempConfig.validateConfig();

            ticketPool = new TicketPool(totalTickets, maxTicketCapacity, numVendors, numCustomers, ticketsPerRelease);
            this.configuration = tempConfig;

            executorService = Executors.newFixedThreadPool(numVendors + numCustomers);

            CountDownLatch vendorLatch = new CountDownLatch(numVendors);

            startVendorsAndCustomers(numVendors, numCustomers, vendorLatch);

            isSystemRunning = true;

            SystemStatus status = getSystemStatus(numVendors, numCustomers);
            eventPublisher.publishEvent(new TicketPoolUpdateEvent(this, status));

            return status;
        } catch (Exception e) {
            System.err.println("Error initializing system: " + e.getMessage());
            throw new IllegalArgumentException("Failed to initialize system", e);
        }
    }

    //create and start vendor and customer threads
    private void startVendorsAndCustomers(int numVendors, int numCustomers, CountDownLatch vendorLatch) {
        vendors.clear();
        customers.clear();

        for (int i = 0; i < numVendors; i++) {
            Vendor vendor = new Vendor(
                    "Vendor" + (i + 1),
                    ticketPool,
                    configuration.getTicketsPerRelease(),
                    vendorLatch,
                    eventPublisher,
                    configuration.getReleaseInterval()
            );
            vendors.add(vendor);
            executorService.submit(vendor);
        }
    }

    //gracefully shutdown the system
    public synchronized void shutdownSystem() {
        if (!isSystemRunning) {
            return;
        }

        vendors.forEach(Vendor::stop);
        customers.forEach(Customer::stop);

        if (executorService != null) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        isSystemRunning = false;
        eventPublisher.publishEvent(new TicketPoolUpdateEvent(this, new SystemStatus()));
    }

    //process a ticket purchase request
    public synchronized String processTicketPurchase() {
        if (ticketPool == null) {
            return null;
        }
        String purchasedTicket = ticketPool.purchaseTicket();
        if (purchasedTicket != null) {
            SystemStatus status = getSystemStatus(vendors.size(), customers.size());
            eventPublisher.publishEvent(new TicketPoolUpdateEvent(this, status));
        }
        return purchasedTicket;
    }

    //process a ticket release request
    public synchronized int processTicketRelease(int ticketsToRelease) {
        if (ticketPool == null) {
            return 0;
        }
        int ticketsAdded = ticketPool.addTickets(ticketsToRelease);
        if (ticketsAdded > 0) {
            SystemStatus status = getSystemStatus(vendors.size(), customers.size());
            eventPublisher.publishEvent(new TicketPoolUpdateEvent(this, status));
        }
        return ticketsAdded;
    }

    //get current system status
    public SystemStatus getSystemStatus(int numVendors, int numCustomers) {
        if (ticketPool == null) {
            return new SystemStatus();
        }
        return new SystemStatus(
                ticketPool.getTotalTickets(),
                ticketPool.getRemainingTickets(),
                configuration.getMaxTicketCapacity(),
                ticketPool.getTicketsPerRelease(),
                numVendors,
                numCustomers
        );
    }

    //get current system state
    public SystemStatus getCurrentState() {
        if (ticketPool == null) {
            return new SystemStatus();
        }
        return getSystemStatus(vendors.size(), customers.size());
    }

    //event class for ticket pool updates
    public static class TicketPoolUpdateEvent {
        private final Object source;
        @Getter
        private final SystemStatus systemStatus;

        public TicketPoolUpdateEvent(Object source, SystemStatus systemStatus) {
            this.source = source;
            this.systemStatus = systemStatus;
        }

        public Object getSource() {
            return source;
        }
    }

    //event class for configuration updates
    public static class ConfigurationUpdateEvent {
        private final Object source;
        @Getter
        private final ConfigurationUpdate configUpdate;

        public ConfigurationUpdateEvent(Object source, ConfigurationUpdate configUpdate) {
            this.source = source;
            this.configUpdate = configUpdate;
        }

        public Object getSource() {
            return source;
        }
    }
}