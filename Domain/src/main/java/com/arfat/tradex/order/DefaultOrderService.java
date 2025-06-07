package com.arfat.tradex.order;

import com.arfat.tradex.order.model.Order;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

class DefaultOrderService implements OrderService {

    private final Map<Long, Order> orders = new ConcurrentHashMap<>();

    @Override
    public Order placeOrder(Order order) {
        order.setId(System.currentTimeMillis());
        orders.put(order.getId(), order);
        return order;
    }

    @Override
    public Order getOrder(long orderId) throws OrderNotFoundException {
        return Optional.ofNullable(orders.get(orderId))
                        .orElseThrow(() -> new OrderNotFoundException("Order with ID " + orderId + " not found."));
    }
}
