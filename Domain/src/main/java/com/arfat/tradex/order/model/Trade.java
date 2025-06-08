package com.arfat.tradex.order.model;

import lombok.Builder;

@Builder
public class Trade {
    private long orderId;
    private double amount;
    private double price;

    public long getOrderId() {
        return orderId;
    }

    public double getAmount() {
        return amount;
    }

    public double getPrice() {
        return price;
    }
}
