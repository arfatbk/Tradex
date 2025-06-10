package com.arfat.tradex.order;

import com.arfat.tradex.order.model.Direction;
import com.arfat.tradex.order.model.Order;
import com.arfat.tradex.order.model.Trade;
import com.arfat.tradex.persistence.Persistence;

import java.util.*;

final class DefaultOrderService implements OrderService {

    private final Persistence persistence;

    DefaultOrderService(Persistence persistence) {
        this.persistence = persistence;
    }

    @Override
    public Order placeOrder(Order order) {
        order.setId(UUID.randomUUID().toString());
        return processOrder(order);
    }

    @Override
    public Order getOrder(String orderId) throws OrderNotFoundException {
        return Optional.ofNullable(persistence.getOrder(orderId))
                .orElseThrow(() -> new OrderNotFoundException("Order with ID " + orderId + " not found."));
    }


    private Trade getTrade(Order order, double assetAmount, double assetPrice) {
        return Trade.builder()
                .orderId(order.getId())
                .amount(assetAmount)
                .price(assetPrice)
                .build();
    }

    private Order processOrder(Order order) {

        var counterOrders = isBuy(order.getDirection()) ? persistence.sellOrders() : persistence.buyOrders();

        var assetOrders = counterOrders.get(order.getAsset());

        if (null != assetOrders) {
            for (var entry : getEntries(order, assetOrders)) {
                matchOrder(order, entry.getValue());
            }
        }

        if (!order.isFullyExecuted()) {
            addOrderIntoState(order);
        }

        persistence.addOrder(order);
        return order;
    }

    private Set<Map.Entry<Double, List<Order>>> getEntries(Order order, NavigableMap<Double, List<Order>> assetOrders) {
        return isBuy(order.getDirection()) ?
                assetOrders.entrySet() :
                assetOrders.descendingMap().entrySet();
    }

    private void matchOrder(Order incomingOrder, List<Order> assetOrders) {
        var iterator = assetOrders.iterator();
        while (iterator.hasNext() && !incomingOrder.isFullyExecuted()) {
            var counterOrder = iterator.next();

            synchronized (this) {
                if (incomingOrder.canMatch(counterOrder)) {
                    double assetAmount = Math.min(incomingOrder.getPendingAmount(), counterOrder.getPendingAmount());
                    double assetPrice = counterOrder.getPrice();

                    Trade buyerTrade = this.getTrade(counterOrder, assetAmount, assetPrice);
                    Trade sellerTrade = this.getTrade(incomingOrder, assetAmount, assetPrice);

                    counterOrder.addTrade(sellerTrade);
                    incomingOrder.addTrade(buyerTrade);

                    persistence.addOrder(counterOrder);

                    //Remove the counterOrder if it is fully executed
                    if (counterOrder.isFullyExecuted()) {
                        iterator.remove();
                    }

                }
            }
        }

    }

    private void addOrderIntoState(Order order) {
        // Determine the state based on the order direction
        var state = isBuy(order.getDirection()) ?
                persistence.buyOrders() :
                persistence.sellOrders();

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

}

