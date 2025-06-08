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
    private String id;
    private final Instant timestamp;
    private final String asset;
    private final double price;
    private final double amount;
    private final Direction direction;
    private double pendingAmount;
    private final List<Trade> trades = new ArrayList<>();

    public Order(String asset, double price, double amount, Direction direction) {
        this.asset = asset;
        this.price = price;
        this.amount = amount;
        this.direction = direction;
        this.timestamp = Instant.now();
        this.pendingAmount = amount;
    }


    public boolean canMatch(Order counterOrder) {
        if (this.asset.equals(counterOrder.getAsset()) &&
            this.direction != counterOrder.getDirection()) {
            if (this.direction == Direction.BUY) {
                return this.price >= counterOrder.price;
            } else {
                return this.price <= counterOrder.price;
            }
        }
        return false;
    }

    public Order addTrade(Trade trade) {
        this.pendingAmount -= trade.getAmount();
        trades.add(trade);
        return this;
    }

    public boolean isFullyExecuted() {
        return 0 == pendingAmount;
    }
}