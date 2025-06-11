package com.arfat.tradex.order.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@ToString
public final class Order {

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
        this.validate(asset, price, amount, direction);
        this.asset = asset;
        this.price = price;
        this.amount = amount;
        this.direction = direction;
        this.timestamp = Instant.now();
        this.pendingAmount = amount;
    }

    private void validate(String asset, double price, double amount, Direction direction) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Order amount must be greater than zero");
        }
        if (price <= 0) {
            throw new IllegalArgumentException("Order price must be greater than zero");
        }
        if (null == asset || asset.isBlank()) {
            throw new IllegalArgumentException("Asset must not be null or empty");
        }
        if (null == direction) {
            throw new IllegalArgumentException("Direction must either BUY or SELL");
        }
    }


    /**
     * Checks if this order can match with the given counterOrder.
     *
     * @param counterOrder the order to check against
     * @return true if this order can match with the counterOrder, false otherwise
     */
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

    /**
     * Adds a trade to this order, also Adjust the pending amount accordingly.
     *
     * @param trade the trade to add
     * @return this order instance for method chaining
     */
    public Order addTrade(Trade trade) {
        this.pendingAmount -= trade.getAmount();
        trades.add(trade);
        return this;
    }

    /**
     * Checks if the order is fully executed.
     *
     * @return true if the pending amount is zero, false otherwise
     */
    public boolean isFullyExecuted() {
        return 0 == pendingAmount;
    }
}