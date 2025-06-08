package com.arfat.tradex.persistence;

import com.arfat.tradex.order.model.Direction;
import com.arfat.tradex.order.model.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NavigableMap;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class StateMachineTest {

    private StateMachine stateMachine;

    @BeforeEach
    void setUp() {
        stateMachine = new StateMachine();
    }

    @Test
    void shouldThrowException_WhenAddingNullOrder() {
        var ex = assertThrows(IllegalArgumentException.class,
                () -> stateMachine.addOrder(null));

        assertEquals("Order must not be null", ex.getMessage());
    }

    @Test
    void shouldStoreAndRetrieveOrder() {
        Order order = createOrder("APL", 100.0, 1, Direction.BUY);
        stateMachine.addOrder(order);

        Order retrievedOrder = stateMachine.getOrder(order.getId());
        assertNotNull(retrievedOrder);
        assertEquals(order.getId(), retrievedOrder.getId());
        assertEquals(order.getAsset(), retrievedOrder.getAsset());
        assertEquals(order.getPrice(), retrievedOrder.getPrice());
        assertEquals(order.getAmount(), retrievedOrder.getAmount());
        assertEquals(order.getDirection(), retrievedOrder.getDirection());
    }

    @Test
    void shouldReturnNull_WhenOrderNotFound() {
        assertNull(stateMachine.getOrder("non-existent-id"));
    }

    @Test
    void shouldReturnEmptyMaps_WhenNoOrders() {
        assertTrue(stateMachine.buyOrders().isEmpty());
        assertTrue(stateMachine.sellOrders().isEmpty());
    }

    @Test
    void shouldGetBuyOrders() {
        Order order = createOrder("APL", 100.0, 1, Direction.BUY);
        stateMachine.buyOrders().computeIfAbsent(order.getAsset(), k -> new java.util.TreeMap<>())
                .computeIfAbsent(order.getPrice(), k -> new java.util.ArrayList<>()).add(order);

        assertNotNull(stateMachine.buyOrders());
        assertFalse(stateMachine.buyOrders().isEmpty());
        assertEquals(1, stateMachine.buyOrders().size());
    }

    @Test
    void shouldGetSellOrders() {
        Order order = createOrder("APL", 100.0, 1, Direction.SELL);
        stateMachine.sellOrders().computeIfAbsent(order.getAsset(), k -> new java.util.TreeMap<>())
                .computeIfAbsent(order.getPrice(), k -> new java.util.ArrayList<>()).add(order);

        assertNotNull(stateMachine.sellOrders());
        assertFalse(stateMachine.sellOrders().isEmpty());
        assertEquals(1, stateMachine.sellOrders().size());

    }

    @Test
    void shouldPreservePriceOrderingInNavigableMap() {
        Order order1 = createOrder("APL", 100.0, 1, Direction.BUY);
        Order order2 = createOrder("APL", 102.0, 1, Direction.BUY);
        Order order3 = createOrder("APL", 101.0, 1, Direction.BUY);

        stateMachine.buyOrders().computeIfAbsent(order1.getAsset(), k -> new java.util.TreeMap<>())
                .computeIfAbsent(order1.getPrice(), k -> new java.util.ArrayList<>()).add(order1);

        stateMachine.buyOrders().computeIfAbsent(order2.getAsset(), k -> new java.util.TreeMap<>())
                .computeIfAbsent(order2.getPrice(), k -> new java.util.ArrayList<>()).add(order2);

        stateMachine.buyOrders().computeIfAbsent(order3.getAsset(), k -> new java.util.TreeMap<>())
                .computeIfAbsent(order3.getPrice(), k -> new java.util.ArrayList<>()).add(order3);


        NavigableMap<Double, List<Order>> buyOrdersForAsset = stateMachine.buyOrders().get("APL");

        assertNotNull(buyOrdersForAsset);
        assertEquals(3, buyOrdersForAsset.size());
        assertEquals(100.0, buyOrdersForAsset.firstKey());
        assertEquals(102.0, buyOrdersForAsset.lastKey());
    }

    @Test
    void shouldSeparateOrdersByAsset() {
        Order aplOrder = createOrder("APL", 100.0, 2, Direction.BUY);
        Order googlOrder = createOrder("GOOGL", 2500.0, 1, Direction.BUY);

        stateMachine.buyOrders().computeIfAbsent(aplOrder.getAsset(), k -> new java.util.TreeMap<>())
                .computeIfAbsent(aplOrder.getPrice(), k -> new java.util.ArrayList<>()).add(aplOrder);

        stateMachine.buyOrders().computeIfAbsent(googlOrder.getAsset(), k -> new java.util.TreeMap<>())
                .computeIfAbsent(googlOrder.getPrice(), k -> new java.util.ArrayList<>()).add(googlOrder);


        assertEquals(2, stateMachine.buyOrders().size());
        assertTrue(stateMachine.buyOrders().containsKey("APL"));
        assertTrue(stateMachine.buyOrders().containsKey("GOOGL"));
    }

    private Order createOrder(String asset, double price, double amount, Direction direction) {
        Order order = new Order(asset, price, amount, direction);
        order.setId(UUID.randomUUID().toString());
        return order;
    }
}