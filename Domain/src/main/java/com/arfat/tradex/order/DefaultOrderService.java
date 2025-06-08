package com.arfat.tradex.order;

import com.arfat.tradex.order.model.Direction;
import com.arfat.tradex.order.model.Order;
import com.arfat.tradex.order.model.Trade;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

class DefaultOrderService implements OrderService {

    private final Map<String, Order> orders = new ConcurrentHashMap<>();
    private final Map<String, NavigableMap<Double, List<Order>>> buyOrders = new ConcurrentHashMap<>();
    private final Map<String, NavigableMap<Double, List<Order>>> sellOrders = new ConcurrentHashMap<>();

    @Override
    public Order placeOrder(Order order) {
        order.setId(UUID.randomUUID().toString());
        return processOrder(order);
    }

    private Order processOrder(Order order) {

        var incomingOrder = order;
        var counterOrders = isBuy(incomingOrder.getDirection()) ? sellOrders : buyOrders;

        var assetOrders = counterOrders.get(incomingOrder.getAsset());

        if (null != assetOrders) {

            var entries = isBuy(order.getDirection()) ?
                    assetOrders.entrySet() :
                    assetOrders.descendingMap().entrySet();

            for (var entry : entries) {
                incomingOrder = matchOrder(incomingOrder, entry.getValue());
            }

        }

        if (!incomingOrder.isFullyExecuted()) {
            addOrderIntoState(incomingOrder);
        }

        orders.put(incomingOrder.getId(), incomingOrder);
        return incomingOrder;
    }

    private Order matchOrder(Order incomingOrder, List<Order> assetOrders) {
        var iterator = assetOrders.iterator();
        while (iterator.hasNext() && !incomingOrder.isFullyExecuted()) {
            var counterOrder = iterator.next();

            if(incomingOrder.canMatch(counterOrder)){
                double assetAmount = Math.min(incomingOrder.getPendingAmount(), counterOrder.getPendingAmount());
                double assetPrice = counterOrder.getPrice();

                Trade buyerTrade = Trade.builder()
                        .orderId(counterOrder.getId())
                        .amount(assetAmount)
                        .price(assetPrice)
                        .build();

                Trade sellerTrade = Trade.builder()
                        .orderId(incomingOrder.getId())
                        .amount(assetAmount)
                        .price(assetPrice)
                        .build();

                var updatedCounterOrder = counterOrder.addTrade(sellerTrade);
                var updatedIncomingOrder = incomingOrder.addTrade(buyerTrade);

                orders.put(updatedCounterOrder.getId(), updatedCounterOrder);

                //Remove the counterOrder if it is fully executed
                if(updatedCounterOrder.isFullyExecuted()){
                    iterator.remove();
                }

                return updatedIncomingOrder;
            }
        }

        return incomingOrder;
    }

    private void addOrderIntoState(Order order) {
        // Determine the state based on the order direction
        var state = isBuy(order.getDirection()) ? buyOrders : sellOrders;

        // Get or create the map for the specific asset
        state.computeIfAbsent(order.getAsset(), k -> new TreeMap<>());
        Map<Double, List<Order>> ordersByPrice = state.get(order.getAsset());

        // Get or create the list for the specific price
        var ordersAtPrice = ordersByPrice.computeIfAbsent(order.getPrice(), k -> new ArrayList<>());
        // Add the order to the list
        ordersAtPrice.add(order);

    }

    private boolean isBuy(Direction direction) {
        return Direction.BUY.equals(direction);
    }

    @Override
    public Order getOrder(String orderId) throws OrderNotFoundException {
        return Optional.ofNullable(orders.get(orderId))
                .orElseThrow(() -> new OrderNotFoundException("Order with ID " + orderId + " not found."));
    }
}
