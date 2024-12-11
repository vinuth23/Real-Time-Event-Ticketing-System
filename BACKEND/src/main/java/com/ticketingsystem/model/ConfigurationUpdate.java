package com.ticketingsystem.model;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

@Setter
public class ConfigurationUpdate {
    @Getter
    @NotNull
    @Min(1)
    private Integer totalTickets;

    @Getter
    @NotNull
    @Min(1)
    private Integer releaseInterval;

    @Getter
    @NotNull(groups = CustomerOperation.class)
    @Min(1)
    private Integer purchaseInterval = 2;

    @Getter
    @NotNull
    @Min(1)
    private Integer ticketsPerRelease;

    @Getter
    @NotNull
    @Min(1)
    private Integer maxTicketCapacity;

    @Getter
    @NotNull
    @Min(0)
    private Integer numVendors = 0;

    @Getter
    @NotNull
    @Min(0)
    private Integer numCustomers = 0;

    public interface CustomerOperation {}
    public interface VendorOperation {}

    public ConfigurationUpdate() {}

    public ConfigurationUpdate(Integer totalTickets, Integer releaseInterval,
                               Integer purchaseInterval, Integer ticketsPerRelease,
                               Integer maxTicketCapacity, Integer numVendors,
                               Integer numCustomers) {
        this.totalTickets = totalTickets;
        this.releaseInterval = releaseInterval;
        this.purchaseInterval = purchaseInterval != null ? purchaseInterval : 2;
        this.ticketsPerRelease = ticketsPerRelease;
        this.maxTicketCapacity = maxTicketCapacity;
        this.numVendors = numVendors != null ? numVendors : 0;
        this.numCustomers = numCustomers != null ? numCustomers : 0;
    }

    @Override
    public String toString() {
        return String.format(
                "ConfigurationUpdate(totalTickets=%d, releaseInterval=%ds, purchaseInterval=%ds, " +
                        "ticketsPerRelease=%d, maxCapacity=%d, numVendors=%d, numCustomers=%d)",
                totalTickets, releaseInterval, purchaseInterval, ticketsPerRelease,
                maxTicketCapacity, numVendors, numCustomers
        );
    }
}