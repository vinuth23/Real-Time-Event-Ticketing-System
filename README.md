# Real-Time Event Ticketing System

A concurrent ticketing system implementing the **Producer-Consumer** pattern to handle ticket sales and purchases in real-time.

## Prerequisites

- **Java**: JDK 11 or higher
- **Maven**: 3.6.3 or higher
- **Node.js**: 14+ and npm
- **Git**: For version control

---

## Installation

### Clone the Repository
```bash
# Clone the repository and navigate to the project directory
git clone https://github.com/vinuth23/Real-Time-Event-Ticketing-System
cd ticketing-system
```

### Install Backend Dependencies
```bash
mvn clean install
```

### Install Frontend Dependencies
```bash
cd frontend
npm install
```

---

## Usage

### CLI Instructions

#### Start the CLI Application:
1. Open the project in your IDE (e.g., IntelliJ IDEA).
2. Ensure **JDK 21** is configured.
3. Build the project.
4. Run `Main.java`.

#### Available Commands:
When prompted, enter the following details:
- **Total Tickets**: Total number of tickets available.
- **Vendor Release Interval**: Interval (in milliseconds) at which vendors release tickets.
- **Tickets per Release**: Number of tickets released per interval.
- **Maximum Ticket Pool Capacity**: Maximum capacity of the ticket pool.
- **Customer Retrieval Rate**: Rate (in milliseconds) at which customers purchase tickets.
- **Number of Vendors**: Number of vendors.
- **Number of Customers**: Number of customers.

### GUI Instructions

#### Start the GUI Application:
1. Open the code in **Visual Studio Code**.
2. Open the terminal by pressing `Ctrl + \``.
3. Run the following command:
   ```bash
   npm start
   ```
   
Alternatively, open a terminal in the code directory and type:
```bash
npm start
```

Runs the app in development mode. Open [http://localhost:3000](http://localhost:3000) to view it in your browser.
The page will reload when you make changes.

#### Modify System Parameters:
- **Total Tickets**: Total number of tickets available.
- **Ticket Release Rate**: Rate at which vendors release tickets.
- **Customer Retrieval Rate**: Rate at which customers can purchase tickets.
- **Max Ticket Capacity**: Maximum capacity of the ticket pool.

To modify these parameters:
1. Click the **settings icon** on the bottom right in the UI.

#### Using the Interface:
- **Vendor Dashboard**: Allows vendors to add tickets into the system.
- **Customer Dashboard**: Allows customers to purchase tickets from the system.
- **Ticket Pool Status**: A progress bar and status updates in real-time.

---

## Running the Backend

1. Open the project in an IDE (e.g., IntelliJ IDEA).
2. Navigate to `src/main/java/com/ticketingsystem/TicketingBackendApplication.java`.
3. Run the application.

---

## Troubleshooting

### System Won't Start

#### Checkpoints:
- Verify all required services are running.
- Ensure the configuration file exists and is valid.
- Check if the ports are already in use.

### Common Errors:

#### "Port Already in Use"
- **Solution**: Change the port in the configuration or stop the conflicting application.

#### "Configuration File Not Found"
- **Solution**: Verify `config.properties` exists in the correct location.

---

## Additional Notes

### Real-Time Updates
The system provides real-time updates on ticket availability and user actions through both the CLI and GUI interfaces.

### System Architecture
Implements the **Producer-Consumer** pattern to efficiently manage concurrent ticket sales and purchases.

