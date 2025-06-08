package com.arfat.tradex.order;

import com.arfat.tradex.order.model.Order;

public interface OrderService {
    Order placeOrder(Order order);

    Order getOrder(String orderId) throws OrderNotFoundException;
}
