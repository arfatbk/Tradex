package com.arfat.tradex.order.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public final class Trade {
    private String orderId;
    private double amount;
    private double price;
}
