package com.arfat.tradex.order;

import com.arfat.tradex.order.model.Direction;
import com.arfat.tradex.order.model.Order;
import com.arfat.tradex.order.model.Trade;
import com.arfat.tradex.persistence.Persistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

final class DefaultOrderService implements OrderService {

    private final static Logger log = LoggerFactory.getLogger(DefaultOrderService.class);
    private final Persistence persistence;

    DefaultOrderService(Persistence persistence) {
        this.persistence = persistence;
    }

    @Override
    public Order placeOrder(Order order) {
        order.setId(UUID.randomUUID().toString());
        log.debug("processing order with id: {}", order.getId());
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
            log.debug("Found counter orders with size: {}", assetOrders.size());
            for (var entry : getSortedPriceEntries(order, assetOrders)) {
                matchOrder(order, entry.getValue());
            }
        } else {
            log.info("No counter orders found for asset: {}", order.getAsset());
        }

        if (!order.isFullyExecuted()) {
            log.debug("Order with ID {} is not fully executed, adding to state.", order.getId());
            addOrderIntoState(order);
        }

        persistence.addOrder(order);
        return order;
    }

    private Set<Map.Entry<Double, List<Order>>> getSortedPriceEntries(Order order, NavigableMap<Double, List<Order>> assetOrders) {
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

                    log.info("Matched order {} with counter order {} for asset {} at price {} with amount {}",
                            incomingOrder.getId(), counterOrder.getId(), incomingOrder.getAsset(), assetPrice, assetAmount);

                    //Remove the counterOrder if it is fully executed
                    if (counterOrder.isFullyExecuted()) {
                        log.info("Counter order {} is fully executed and will be removed from state.", counterOrder.getId());
                        iterator.remove();
                    }
                }
            }
        }

    }

    /**
     * Adds the order into the state based on its direction (buy or sell).
     * This state is used to match orders
     *
     * @param order the order to be added
     */
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

        log.debug("ordersAtPrice size {}", ordersAtPrice.size());
        // Add the order to the list
        ordersAtPrice.add(order);

    }

    private boolean isBuy(Direction direction) {
        return Direction.BUY.equals(direction);
    }

}

