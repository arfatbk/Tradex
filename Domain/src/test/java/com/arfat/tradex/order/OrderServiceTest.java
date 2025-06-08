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
        orderService = new DefaultOrderService(new StateMachineStub());
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

        var orderId = "WRONG_ID";
        var ex = assertThrows(OrderNotFoundException.class, () -> orderService.getOrder(orderId));
        String expected = String.format("Order with ID %s not found.", orderId);
        assertEquals(expected, ex.getMessage());

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
    void shouldReturnPendingAmountZero_WhenOrderIsMatched() {
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
    void shouldReturnValidOrderState_WhenOrderIsFullyMatched() {
        Order buy = orderService.placeOrder(createBuyOrder("APL", 1500.0, 2));
        Order sell = orderService.placeOrder(createSellOrder("APL", 1500.0, 2));

        Order fetchedBuyOrder = orderService.getOrder(buy.getId());
        Order fetchedSellOrder = orderService.getOrder(sell.getId());

        assertTrue(fetchedBuyOrder.isFullyExecuted());
        assertTrue(fetchedSellOrder.isFullyExecuted());

        assertEquals(1, fetchedBuyOrder.getTrades().size());
        assertEquals(1, fetchedSellOrder.getTrades().size());
    }


    @Test
    void shouldReturnCorrectState_WhenOrderIsPartiallyMatched() {
        Order buy = orderService.placeOrder(createBuyOrder("APL", 1500.0, 2));
        Order sell = orderService.placeOrder(createSellOrder("APL", 1500.0, 1));

        Order fetchedBuyOrder = orderService.getOrder(buy.getId());
        Order fetchedSellOrder = orderService.getOrder(sell.getId());

        assertEquals(1, fetchedBuyOrder.getPendingAmount());
        assertEquals(0, fetchedSellOrder.getPendingAmount());

        assertEquals(1, fetchedBuyOrder.getTrades().size());
        assertEquals(1, fetchedSellOrder.getTrades().size());
    }


    @Test
    void shouldReturnCorrectState_WhenMultipleOrdersFulfillOneOrder() {
        Order buy = orderService.placeOrder(createBuyOrder("APL", 1500.0, 2));
        Order sell1 = orderService.placeOrder(createSellOrder("APL", 1500.0, 1));
        Order sell2 = orderService.placeOrder(createSellOrder("APL", 1500.0, 1));

        Order fetchedBuyOrder = orderService.getOrder(buy.getId());
        Order fetchedSell1Order = orderService.getOrder(sell1.getId());
        Order fetchedSell2Order = orderService.getOrder(sell2.getId());

        assertEquals(0, fetchedBuyOrder.getPendingAmount());
        assertEquals(0, fetchedSell1Order.getPendingAmount());
        assertEquals(0, fetchedSell2Order.getPendingAmount());

        assertEquals(2, fetchedBuyOrder.getTrades().size());
        assertEquals(1, fetchedSell1Order.getTrades().size());
        assertEquals(1, fetchedSell2Order.getTrades().size());
    }

    @Test
    void shouldMatchWithBestPrice() {
        // Place two sell orders at different prices
        Order sell1 = orderService.placeOrder(createSellOrder("APL", 1400.0, 1));
        Order sell2 = orderService.placeOrder(createSellOrder("APL", 1300.0, 1));
        // Place a buy order that can match both
        Order buy = orderService.placeOrder(createBuyOrder("APL", 1500.0, 1));

        Order fetchedBuyOrder = orderService.getOrder(buy.getId());
        Order fetchedSell1Order = orderService.getOrder(sell1.getId());
        Order fetchedSell2Order = orderService.getOrder(sell2.getId());

        // The buy order should match with the lowest price sell order (1300.0)
        assertEquals(0, fetchedBuyOrder.getPendingAmount());
        assertEquals(1, fetchedBuyOrder.getTrades().size());
        assertEquals(0, fetchedSell2Order.getPendingAmount()); // sell2 (1300.0) should be matched
        assertEquals(1, fetchedSell2Order.getTrades().size());

        assertEquals(1, fetchedSell1Order.getPendingAmount()); // sell1 (1400.0) should remain untouched
        assertEquals(0, fetchedSell1Order.getTrades().size());
    }

    @Test
    void shouldMatchBuyOrdersWithBestPriceFirst() {
        // Place two buy orders at different prices
        Order buy1 = orderService.placeOrder(createBuyOrder("APL", 1300.0, 1));
        Order buy2 = orderService.placeOrder(createBuyOrder("APL", 1500.0, 1));
        // Place a sell order that can match both
        Order sell = orderService.placeOrder(createSellOrder("APL", 1200.0, 1));

        // Should match with the highest buy price first (1500.0)
        Order fetchedBuy1Order = orderService.getOrder(buy1.getId());
        Order fetchedBuy2Order = orderService.getOrder(buy2.getId());
        Order fetchedSellOrder = orderService.getOrder(sell.getId());

        assertEquals(1, fetchedBuy1Order.getPendingAmount()); // Lower price should remain unmatched
        assertEquals(0, fetchedBuy2Order.getPendingAmount()); // Higher price should be matched
        assertEquals(0, fetchedSellOrder.getPendingAmount());
    }

    @Test
    void shouldHandleExactPriceMatch() {
        Order buy = orderService.placeOrder(createBuyOrder("APL", 1500.0, 1));
        Order sell1 = orderService.placeOrder(createSellOrder("APL", 1501.0, 1)); // Should not match
        Order sell2 = orderService.placeOrder(createSellOrder("APL", 1500.0, 1)); // Should match exactly

        Order fetchedBuyOrder = orderService.getOrder(buy.getId());
        Order fetchedSell1Order = orderService.getOrder(sell1.getId());
        Order fetchedSell2Order = orderService.getOrder(sell2.getId());

        assertEquals(0, fetchedBuyOrder.getPendingAmount());
        assertEquals(1, fetchedSell1Order.getPendingAmount());
        assertEquals(0, fetchedSell2Order.getPendingAmount());
    }


    @Test
    void shouldHandleMultiplePartialMatches() {
        // Place a large buy order
        Order buy = orderService.placeOrder(createBuyOrder("APL", 1500.0, 3));

        // Place multiple smaller sell orders
        Order sell1 = orderService.placeOrder(createSellOrder("APL", 1500.0, 1));
        Order sell2 = orderService.placeOrder(createSellOrder("APL", 1500.0, 1));
        Order sell3 = orderService.placeOrder(createSellOrder("APL", 1500.0, 0.5));

        Order fetchedBuyOrder = orderService.getOrder(buy.getId());
        Order fetchedSell1Order = orderService.getOrder(sell1.getId());
        Order fetchedSell2Order = orderService.getOrder(sell2.getId());
        Order fetchedSell3Order = orderService.getOrder(sell3.getId());

        assertEquals(0.5, fetchedBuyOrder.getPendingAmount());
        assertEquals(3, fetchedBuyOrder.getTrades().size());
        assertEquals(0, fetchedSell1Order.getPendingAmount());
        assertEquals(0, fetchedSell2Order.getPendingAmount());
        assertEquals(0, fetchedSell3Order.getPendingAmount());
    }

    @Test
    void shouldMatchOrdersInPriceTimeOrder() {
        // Place sell orders in sequence
        Order sell1 = orderService.placeOrder(createSellOrder("APL", 1500.0, 1)); // First at 1500
        Order sell2 = orderService.placeOrder(createSellOrder("APL", 1500.0, 1)); // Second at 1500
        Order sell3 = orderService.placeOrder(createSellOrder("APL", 1400.0, 1)); // Better price

        // Place buy order that can match with any
        Order buy = orderService.placeOrder(createBuyOrder("APL", 1500.0, 1));

        Order fetchedBuyOrder = orderService.getOrder(buy.getId());
        Order fetchedSell1Order = orderService.getOrder(sell1.getId());
        Order fetchedSell2Order = orderService.getOrder(sell2.getId());
        Order fetchedSell3Order = orderService.getOrder(sell3.getId());

        // Should match with sell3 due to best price
        assertEquals(0, fetchedBuyOrder.getPendingAmount());
        assertEquals(1, fetchedSell1Order.getPendingAmount());
        assertEquals(1, fetchedSell2Order.getPendingAmount());
        assertEquals(0, fetchedSell3Order.getPendingAmount());
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
