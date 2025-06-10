package com.arfat.tradex.model;

import com.arfat.tradex.order.model.Trade;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;
import java.util.List;

/**
 * Represents the response contract for an order.
 * By not using OrderResponse from production code will ensure that we did not break the contract accidentally.
 */
@Getter
@Setter
@ToString
public class OrderResponseContract {

    private String id;
    private Instant timestamp;
    private String asset;
    private double price;
    private double amount;
    private Direction direction;
    private List<Trade> trades;
    private double pendingAmount;

    public enum Direction {
        SELL, BUY
    }
}


