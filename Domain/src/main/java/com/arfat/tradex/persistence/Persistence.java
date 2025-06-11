package com.arfat.tradex.persistence;

import com.arfat.tradex.order.model.Order;

import java.util.List;
import java.util.Map;
import java.util.NavigableMap;

/**
 * Persistence interface for managing orders. <br>
 * It can be easily replaced with a different implementation that supports
 * distributed transactions in case of SOA architecture,
 * implement permanent storage of some kind.
 */
public interface Persistence {
    Map<String, NavigableMap<Double, List<Order>>> sellOrders();

    Map<String, NavigableMap<Double, List<Order>>> buyOrders();

    /**
     * Adds an order to the persistence layer. Used by getOrder(id)
     *
     * @param order the order to be added
     */
    void addOrder(Order order);

    Order getOrder(String orderId);
}
