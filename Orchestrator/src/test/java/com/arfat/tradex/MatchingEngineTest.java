package com.arfat.tradex;

import com.arfat.tradex.order.OrderNotFoundException;
import com.arfat.tradex.order.OrderService;
import com.arfat.tradex.order.OrdersController;
import com.arfat.tradex.order.model.Direction;
import com.arfat.tradex.order.model.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@WebMvcTest(OrdersController.class)
public class MatchingEngineTest {

    @Autowired
    private MockMvcTester mockMvc;

    @MockitoBean
    private OrderService service;

    @Test
    void shouldGiveErrorResponse_WhenOrderNotFound() {
        var id = 1;
        when(service.getOrder(any())).thenThrow(
                new OrderNotFoundException(String.format("Order with ID %s not found.", id))
        );

        mockMvc
                .get()
                .uri("/orders/{id}", id)
                .exchange()
                .assertThat()
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson()
                .isEqualTo("""
                        {
                            "message": "Order with ID %s not found.",
                            "status": 400
                        }
                        """.formatted(id));

        verify(service, times(1)).getOrder(any());
    }

    @Test
    void shouldReturnOrder_WhenOrderExists() {
        var id = "123";
        var order = new Order("BTC", 50000, 1, Direction.BUY);
        order.setId(id);
        when(service.getOrder(any())).thenReturn(order);

        mockMvc
                .get()
                .uri("/orders/{id}", id)
                .exchange()
                .assertThat()
                .hasStatusOk()
                .bodyJson()
                .isLenientlyEqualTo("""
                        {
                            "id":"%s",
                            "asset":"BTC",
                            "price":50000.0,
                            "amount":1.0,
                            "direction":"BUY",
                            "pendingAmount":1.0,
                            "trades":[]}
                        """.formatted(id));

        verify(service, times(1)).getOrder(any());

    }

    @Test
    void shouldSubmitOrder() {

        Order order = new Order("BTC", 43250.00, 0.25, Direction.BUY);
        order.setId("1234567");

        when(service.placeOrder(any()))
                .thenReturn(order);
        mockMvc
                .post()
                .uri("/orders")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content("""
                            {
                                "asset": "BTC",
                                "price": 43250.00,
                                "amount": 0.25,
                                "direction": "BUY"
                            }
                        """)
                .exchange()
                .assertThat()
                .hasStatus(HttpStatus.CREATED);

        verify(service, times(1)).placeOrder(any());

    }

}
