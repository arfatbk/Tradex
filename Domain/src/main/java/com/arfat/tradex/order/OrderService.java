package com.arfat.tradex.order;

import com.arfat.tradex.order.model.Order;

public interface OrderService {
    /**
     * Places an order in the system. The order will be processed immediately.
     * This method will match the order with existing orders in the system
     * and return the updated order with trades.
     * Order may or may not be fully matched, depending on the available counter orders.
     *
     * @param order The order to be placed.
     * @return Order state
     */
    Order placeOrder(Order order);

    /**
     * Retrieves an order by its ID.
     * @param orderId The ID of the order to retrieve.
     * @return The order with the specified ID.
     * @throws OrderNotFoundException if no order with the specified ID exists.
     */
    Order getOrder(String orderId) throws OrderNotFoundException;
}
