package com.arfat.tradex.order;

import com.arfat.tradex.order.model.Direction;
import com.arfat.tradex.order.model.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

public class OrderServiceTest {

    private OrderService orderService;

    @BeforeEach
    void init() {
        orderService = new DefaultOrderService();
    }

    @ParameterizedTest
    @EnumSource(Direction.class)
    void shouldCreateAnOrder(Direction direction) {

        Order orderRequest = isBuy(direction) ?
                createBuyOrder("APL", 100.0, 1) :
                createSellOrder("APL", 100.0, 1);

        Order order = orderService.placeOrder(orderRequest);
        assertNotNull(order);
        assertEquals("APL", order.getAsset());
        assertEquals(100.0, order.getPrice());
        assertEquals(1.0, order.getAmount());
        assertEquals(isBuy(direction) ? Direction.BUY : Direction.SELL, order.getDirection());
        assertEquals(1.0, order.getPendingAmount());
        assertEquals(0, order.getTrades().size());
    }


    @Test
    void shouldThrowExceptionWhenOrderNotFound() {

        var ex = assertThrows(OrderNotFoundException.class, () -> orderService.getOrder(1));
        assertEquals("Order with ID 1 not found.", ex.getMessage());

    }

    @Test
    void shouldReturnOrderById() {
        Order order = orderService.placeOrder(createBuyOrder("GOOGL", 1500.0, 2));
        Order fetchedOrder = orderService.getOrder(order.getId());
        assertNotNull(fetchedOrder);
    }


    @Test
    void shouldNotMatch_WhenAssetIsDifferent() {
        Order buy = orderService.placeOrder(createBuyOrder("APL", 1500.0, 2));
        Order sell = orderService.placeOrder(createSellOrder("GOOGL", 1500.0, 2));

        Order fetchedBuyOrder = orderService.getOrder(buy.getId());
        Order fetchedSellOrder = orderService.getOrder(sell.getId());

        assertEquals(2, fetchedBuyOrder.getPendingAmount());
        assertEquals(2, fetchedSellOrder.getPendingAmount());

        assertEquals(0, fetchedBuyOrder.getTrades().size());
        assertEquals(0, fetchedSellOrder.getTrades().size());

    }

    @Test
    void shouldNotMatch_WhenPriceDifferent() {
        Order buy = orderService.placeOrder(createBuyOrder("APL", 1400.0, 2));
        Order sell = orderService.placeOrder(createSellOrder("APL", 1500.0, 2));

        Order fetchedBuyOrder = orderService.getOrder(buy.getId());
        Order fetchedSellOrder = orderService.getOrder(sell.getId());

        assertEquals(2, fetchedBuyOrder.getPendingAmount());
        assertEquals(2, fetchedSellOrder.getPendingAmount());

        assertEquals(0, fetchedBuyOrder.getTrades().size());
        assertEquals(0, fetchedSellOrder.getTrades().size());

    }

    @Test
    void shouldMatch_WhenPriceAndAssetSame() {
        Order buy = orderService.placeOrder(createBuyOrder("APL", 1500.0, 2));
        Order sell = orderService.placeOrder(createSellOrder("APL", 1500.0, 2));

        Order fetchedBuyOrder = orderService.getOrder(buy.getId());
        Order fetchedSellOrder = orderService.getOrder(sell.getId());

        assertEquals(0, fetchedBuyOrder.getPendingAmount());
        assertEquals(0, fetchedSellOrder.getPendingAmount());

        assertEquals(1, fetchedBuyOrder.getTrades().size());
        assertEquals(1, fetchedSellOrder.getTrades().size());
    }

    @Test
    void shouldReturnPendingAmountZero_WhenOrderIsMatched(){
        Order buy = orderService.placeOrder(createBuyOrder("APL", 1500.0, 2));
        Order sell = orderService.placeOrder(createSellOrder("APL", 1500.0, 2));

        Order fetchedBuyOrder = orderService.getOrder(buy.getId());
        Order fetchedSellOrder = orderService.getOrder(sell.getId());

        assertEquals(0, fetchedBuyOrder.getPendingAmount());
        assertEquals(0, fetchedSellOrder.getPendingAmount());

        assertEquals(1, fetchedBuyOrder.getTrades().size());
        assertEquals(1, fetchedSellOrder.getTrades().size());

        assertEquals(0, fetchedBuyOrder.getPendingAmount());
        assertEquals(0, fetchedSellOrder.getPendingAmount());
    }

    @Test
    void shouldReturnValidOrderState_WhenOrderIsFullyMatched(){
        Order buy = orderService.placeOrder(createBuyOrder("APL", 1500.0, 2));
        Order sell = orderService.placeOrder(createSellOrder("APL", 1500.0, 2));

        Order fetchedBuyOrder = orderService.getOrder(buy.getId());
        Order fetchedSellOrder = orderService.getOrder(sell.getId());

        assertTrue(fetchedBuyOrder.isFullyExecuted());
        assertTrue(fetchedSellOrder.isFullyExecuted());

        //TODO: Flaky test, orderID is currentTimeMillis hence getOrderBy id matching wrong order
        // Both orders are having the same id
        assertEquals(1, fetchedBuyOrder.getTrades().size());
        assertEquals(1, fetchedSellOrder.getTrades().size());
    }


    @Test
    void shouldReturnCorrectState_WhenOrderIsPartiallyMatched() {
        Order buy = orderService.placeOrder(createBuyOrder("APL", 1500.0, 2));
        Order sell1 = orderService.placeOrder(createSellOrder("APL", 1500.0, 1));

        Order fetchedBuyOrder = orderService.getOrder(buy.getId());
        Order fetchedSell1Order = orderService.getOrder(sell1.getId());

        assertEquals(1, fetchedBuyOrder.getPendingAmount());
        assertEquals(0, fetchedSell1Order.getPendingAmount());

        assertEquals(1, fetchedBuyOrder.getTrades().size());
        assertEquals(1, fetchedSell1Order.getTrades().size());
    }


    private Order createBuyOrder(String asset, double price, double amount) {
        return new Order(asset, price, amount, Direction.BUY);
    }

    private Order createSellOrder(String asset, double price, double amount) {
        return new Order(asset, price, amount, Direction.SELL);
    }

    private static boolean isBuy(Direction direction) {
        return direction == Direction.BUY;
    }
}
