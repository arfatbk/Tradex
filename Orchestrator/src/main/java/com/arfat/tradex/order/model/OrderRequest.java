package com.arfat.tradex.order.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OrderRequest {
    @NotEmpty
    private String asset;
    @NotNull
    private double price;
    @NotNull
    @Positive
    private double amount;
    @NotNull
    private Direction direction;

    public static Order toOrder(OrderRequest orderRequest) {
        return new Order(orderRequest.getAsset(),
                orderRequest.getPrice(),
                orderRequest.getAmount(),
                orderRequest.getDirection());
    }
}
