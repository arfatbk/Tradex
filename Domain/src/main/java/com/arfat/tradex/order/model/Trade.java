package com.arfat.tradex.order.model;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public final class Trade {
    private String orderId;
    private double amount;
    private double price;
}
