package com.arfat.tradex.order;

import com.arfat.tradex.order.model.Direction;
import com.arfat.tradex.order.model.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class OrderServiceTest {

    private OrderService orderService;

    @BeforeEach
    void init() {
        orderService = new DefaultOrderService();
    }

    @Test
    void shouldCreateBuyOrder() {
        Order order = orderService.placeOrder(createBuyOrder("APL", 100.0, 1));
        assertNotNull(order);
        assertEquals("APL", order.getAsset());
        assertEquals(100.0, order.getPrice());
        assertEquals(1.0, order.getAmount());
        assertEquals(Direction.BUY, order.getDirection());
        assertEquals(1.0, order.getPendingAmount());
        assertEquals(0, order.getTrades().size());
    }

    @Test
    void shouldCreateSellOrder() {
        Order order = orderService.placeOrder(createSellOrder("APL", 100.0, 1));
        assertNotNull(order);
        assertEquals("APL", order.getAsset());
        assertEquals(100.0, order.getPrice());
        assertEquals(1.0, order.getAmount());
        assertEquals(Direction.SELL, order.getDirection());
        assertEquals(1.0, order.getPendingAmount());
        assertEquals(0, order.getTrades().size());
    }

    @Test

    void shouldThrowExceptionWhenOrderNotFound(){

        var ex = assertThrows(OrderNotFoundException.class, () -> orderService.getOrder(1));
        assertEquals("Order with ID 1 not found.", ex.getMessage());

    }

    @Test
    void shouldReturnOrderById() {
        Order order = orderService.placeOrder(createBuyOrder("GOOGL", 1500.0, 2));
        Order fetchedOrder = orderService.getOrder(order.getId());
        assertNotNull(fetchedOrder);
    }


    private Order createBuyOrder(String asset, double price, double amount) {
        return new Order(asset, price, amount, Direction.BUY);
    }

    private Order createSellOrder(String asset, double price, double amount) {
        return new Order(asset, price, amount, Direction.SELL);
    }
}
