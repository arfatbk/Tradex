package com.arfat.tradex.order.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
@Setter
public class OrderResponse {
    private final String id;
    private final Instant timestamp;
    private final String asset;
    private final double price;
    private final double amount;
    private final Direction direction;
    private final double pendingAmount;
    @Builder.Default
    private final List<Trade> trades = new ArrayList<>();

    public static OrderResponse from(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .timestamp(order.getTimestamp())
                .asset(order.getAsset())
                .price(order.getPrice())
                .amount(order.getAmount())
                .direction(order.getDirection())
                .pendingAmount(order.getPendingAmount())
                .trades(order.getTrades())
                .build();
    }
}
