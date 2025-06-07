package com.arfat.tradex.order;

import com.arfat.tradex.order.model.Order;

class DefaultOrderService implements OrderService {

    @Override
    public Order placeOrder(Order order) {
        return order;
    }

    @Override
    public Order getOrder(long orderId) throws OrderNotFoundException {
        throw new OrderNotFoundException("Order with ID " + orderId + " not found.");
    }
}
