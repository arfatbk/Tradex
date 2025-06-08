package com.arfat.tradex.order.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrderTest {


    @Test
    void shouldNotAllowInvalidOrderCreation() {
        var ex = assertThrows(IllegalArgumentException.class,
                () -> new Order("", 1500.0, 1, Direction.BUY));
        assertEquals("Asset must not be null or empty", ex.getMessage());

        ex = assertThrows(IllegalArgumentException.class,
                () -> new Order(null, 1500.0, 1, Direction.BUY));
        assertEquals("Asset must not be null or empty", ex.getMessage());

        ex = assertThrows(IllegalArgumentException.class,
                () -> new Order("APL", -1.0, 1, Direction.BUY));
        assertEquals("Order price must be greater than zero", ex.getMessage());

        ex = assertThrows(IllegalArgumentException.class,
                () -> new Order("APL", 1500.0, -1, Direction.BUY));
        assertEquals("Order amount must be greater than zero", ex.getMessage());


        ex = assertThrows(IllegalArgumentException.class,
                () -> new Order("APL", 1500.0, 4, null));
        assertEquals("Direction must either BUY or SELL", ex.getMessage());

    }


    @Test
    void shouldCreateValidOrder() {
        Order order = new Order("APL", 1500.0, 4, Direction.BUY);
        assertNotNull(order);
        assertEquals("APL", order.getAsset());
        assertEquals(1500.0, order.getPrice());
        assertEquals(4, order.getAmount());
        assertEquals(Direction.BUY, order.getDirection());
    }

    @Test
    void shouldReturnTrueWhenOrdersCanMatch() {
        Order buyOrder = new Order("APL", 1500.0, 4, Direction.BUY);
        Order sellOrder = new Order("APL", 1499.0, 2, Direction.SELL);
        assertTrue(buyOrder.canMatch(sellOrder));

        Order anotherSellOrder = new Order("APL", 1501.0, 2, Direction.SELL);
        assertFalse(buyOrder.canMatch(anotherSellOrder));
    }

    @Test
    void shouldReturnFalseWhenOrdersCannotMatch() {
        // different assets
        Order buyOrder = new Order("APL", 1500.0, 4, Direction.BUY);
        Order sellOrder = new Order("GOOGL", 1500.0, 2, Direction.SELL);
        assertFalse(buyOrder.canMatch(sellOrder));

        // same asset but same direction
        Order anotherBuyOrder = new Order("APL", 1500.0, 2, Direction.BUY);
        assertFalse(buyOrder.canMatch(anotherBuyOrder));
    }


    @Test
    void shouldAddATradeToOrder() {
        Order order = new Order("APL", 1500.0, 4, Direction.BUY);
        Trade trade = Trade.builder()
                .orderId("trade1")
                .amount(2)
                .price(1500.0)
                .build();

        order.addTrade(trade);

        assertEquals(1, order.getTrades().size());
        assertEquals(trade, order.getTrades().getFirst());
    }

    @Test
    void shouldReturnFullyExecutedStatus() {
        Order order = new Order("APL", 1500.0, 4, Direction.BUY);
        assertFalse(order.isFullyExecuted());

        Trade trade1 = Trade.builder()
                .orderId("trade1")
                .amount(2)
                .price(1500.0)
                .build();
        order.addTrade(trade1);
        assertFalse(order.isFullyExecuted());

        Trade trade2 = Trade.builder()
                .orderId("trade2")
                .amount(2)
                .price(1500.0)
                .build();
        order.addTrade(trade2);
        assertTrue(order.isFullyExecuted());
    }
}