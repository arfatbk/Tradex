package com.arfat.tradex.order;

import com.arfat.tradex.order.model.Order;
import com.arfat.tradex.persistence.Persistence;

import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.concurrent.ConcurrentHashMap;

public class StateMachineStub implements Persistence {
    private final Map<String, Order> orders = new ConcurrentHashMap<>();
    private final Map<String, NavigableMap<Double, List<Order>>> buyOrders = new ConcurrentHashMap<>();
    private final Map<String, NavigableMap<Double, List<Order>>> sellOrders = new ConcurrentHashMap<>();

    @Override
    public Map<String, NavigableMap<Double, List<Order>>> buyOrders() {
        return this.buyOrders;
    }

    @Override
    public Map<String, NavigableMap<Double, List<Order>>> sellOrders() {
        return this.sellOrders;
    }


    @Override
    public void addOrder(Order incomingOrder) {
        if (incomingOrder == null) {
            throw new IllegalArgumentException("Order must not be null");
        }
        this.orders.put(incomingOrder.getId(), incomingOrder);
    }

    @Override
    public Order getOrder(String orderId) {
        return this.orders.get(orderId);
    }

}
