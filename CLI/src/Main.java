import com.coursework.ticketingsystem.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.logging.ConsoleHandler;
import java.util.logging.SimpleFormatter;

public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    private static final AtomicBoolean running = new AtomicBoolean(true);

    public static void main(String[] args) {
        setupLogging();
        Scanner scanner = new Scanner(System.in);

        LOGGER.info("Welcome to the Real-Time Event Ticketing System.");
        System.out.println("Please configure the system parameters:\n");

        try {
            // Get all inputs first
            System.out.println("System Configuration:");
            System.out.println("--------------------");
            int totalTickets = getValidInput(scanner, "Enter total tickets: ");
            int releaseInterval = getValidInput(scanner, "Enter Vendor Release Interval (ms): ");
            int ticketsPerRelease = getValidInput(scanner, "Enter tickets per release: ");
            int maxTicketCapacity = getValidInput(scanner, "Enter max ticket pool capacity: ");
            int customerRetrievalRate = getValidInput(scanner, "Enter Customer Retrieval Rate (ms): ");
            System.out.println("\nThread Configuration:");
            System.out.println("--------------------");
            int numVendors = getValidInput(scanner, "Enter number of vendors: ");
            int numCustomers = getValidInput(scanner, "Enter number of customers: ");

            // Create configuration
            Configuration config = new Configuration(
                    totalTickets,
                    releaseInterval,
                    ticketsPerRelease,
                    maxTicketCapacity,
                    customerRetrievalRate
            );

            System.out.println("\nStarting ticket system...");

            // Create ticket pool using configuration values
            TicketPool ticketPool = new TicketPool(
                    config.getTotalTickets(),
                    config.getMaxTicketCapacity()
            );

            ExecutorService executorService = Executors.newFixedThreadPool(numVendors + numCustomers);

            // Create and start vendor threads
            List<Vendor> vendors = new ArrayList<>();
            for (int i = 0; i < numVendors; i++) {
                Vendor vendor = new Vendor(
                        "Vendor" + (i + 1),
                        ticketPool,
                        config.getTicketsPerRelease(),
                        running,
                        config.getReleaseInterval()
                );
                vendors.add(vendor);
                executorService.submit(vendor);
            }

            // Create and start customer threads
            List<Customer> customers = new ArrayList<>();
            for (int i = 0; i < numCustomers; i++) {
                Customer customer = new Customer(
                        "Customer" + (i + 1),
                        ticketPool,
                        running,
                        config.getCustomerRetrievalRate()
                );
                customers.add(customer);
                executorService.submit(customer);
            }

            // Let the system run continuously
            Thread.sleep(Long.MAX_VALUE);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in ticket system execution", e);
        }
    }

    private static void setupLogging() {
        Logger rootLogger = Logger.getLogger("");
        for (java.util.logging.Handler handler : rootLogger.getHandlers()) {
            rootLogger.removeHandler(handler);
        }
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new SimpleFormatter());
        rootLogger.addHandler(handler);
    }

    private static int getValidInput(Scanner scanner, String prompt) {
        while (true) {
            try {
                System.out.println(prompt);
                String input = scanner.nextLine().trim();
                int value = Integer.parseInt(input);
                if (value <= 0) {
                    throw new IllegalArgumentException("Please enter a positive value.");
                }
                return value;
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid integer.");
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }
    }
}