package com.arfat.tradex.order.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@ToString
public class Order {

    @Setter
    private long id;
    private final Instant timestamp;
    private final String asset;
    private final double price;
    private final double amount;
    private final Direction direction;
    private final double pendingAmount;
    private final List<Trade> trades = new ArrayList<>();

    public Order(String asset, double price, double amount, Direction direction) {
        this.asset = asset;
        this.price = price;
        this.amount = amount;
        this.direction = direction;
        this.timestamp = Instant.now();
        this.pendingAmount = amount;
    }
}