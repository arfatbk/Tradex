package com.arfat.tradex;

import com.arfat.tradex.model.OrderResponseContract;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class MatchingEngineTestIT {

    @LocalServerPort
    private int port;

    private RestClient restClient;

    @BeforeEach
    void init() {
        this.restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();
    }


    @Test
    void shouldPlaceOrder() {
        var response = placeOrder(43251.00, 1.0, OrderResponseContract.Direction.SELL);

        var location = getLocationHeader(response);

        assertTrue(location.isPresent());
        var id = location.get().get(0);

        var order = getOrder(id);

        assertEquals(id, order.getId());
        assertEquals("BTC", order.getAsset());
        assertEquals(43251.0, order.getPrice());
        assertEquals(1.0, order.getAmount());
        assertEquals(OrderResponseContract.Direction.SELL, order.getDirection());
        assertEquals(1.0, order.getPendingAmount());
        assertTrue(order.getTrades().isEmpty());

    }


    @Test
    void OrderMatchingE2ETest() {


        //Sell order #0
        var sellOrder0 = placeOrder(43251.00, 1.0, OrderResponseContract.Direction.SELL);

        var location = getLocationHeader(sellOrder0);
        assertTrue(location.isPresent());
        var sellOrder0Id = location.get().get(0);

        //Buy order #0
        var buyOrder0 = placeOrder(43250.00, 0.25, OrderResponseContract.Direction.BUY);

        location = getLocationHeader(buyOrder0);
        assertTrue(location.isPresent());
        var buyOrder0Id = location.get().get(0);

        //Since we currently don't have a sell order with price satisfying this buy order, it just ends up in our order book.

        var sellOrder0Response = getOrder(sellOrder0Id);
        assertEquals(1.0, sellOrder0Response.getPendingAmount());
        assertEquals(0, sellOrder0Response.getTrades().size());

        //Buy order #1
        var buyOrder1 = placeOrder(43253.00, 0.35, OrderResponseContract.Direction.BUY);
        location = getLocationHeader(buyOrder1);
        assertTrue(location.isPresent());
        var buyOrder1Id = location.get().get(0);

        var buyOrder1Response = getOrder(buyOrder1Id);
        assertEquals(0.0, buyOrder1Response.getPendingAmount());
        assertEquals(1, buyOrder1Response.getTrades().size());


        sellOrder0Response = getOrder(sellOrder0Id);
        assertEquals(0.65, sellOrder0Response.getPendingAmount());
        assertEquals(1, sellOrder0Response.getTrades().size());


        //Last order to fulfill the remaining amount of sell order #0

        //Buy order #2
        var buyOrder2 = placeOrder(43251.00, 0.65, OrderResponseContract.Direction.BUY);
        location = getLocationHeader(buyOrder2);
        assertTrue(location.isPresent());
        var buyOrder2Id = location.get().get(0);

        var buyOrder2Response = getOrder(buyOrder2Id);
        assertEquals(0.0, buyOrder2Response.getPendingAmount());
        assertEquals(1, buyOrder2Response.getTrades().size());

        sellOrder0Response = getOrder(sellOrder0Id);
        assertEquals(0.0, sellOrder0Response.getPendingAmount());
        assertEquals(2, sellOrder0Response.getTrades().size());


        //verify that Buy order #0 is still unmatched
        var buyOrder0Response = getOrder(buyOrder0Id);
        assertEquals(0.25, buyOrder0Response.getPendingAmount());
        assertEquals(0, buyOrder0Response.getTrades().size());

    }

    private ResponseEntity<Void> placeOrder(double price, double amount, OrderResponseContract.Direction direction) {
        return restClient
                .post()
                .uri("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {
                             "asset": "BTC",
                             "price": %s,
                             "amount": %s,
                             "direction": "%s"
                         }
                        """.formatted(price, amount, direction))
                .retrieve()
                .toEntity(Void.class);
    }


    private OrderResponseContract getOrder(String id) {
        return restClient
                .get()
                .uri("/orders/{id}", id)
                .retrieve()
                .body(OrderResponseContract.class);
    }


    private Optional<List<String>> getLocationHeader(ResponseEntity<Void> response) {
        var location = response.getHeaders()
                .entrySet()
                .stream()
                .filter(h -> h.getKey().equalsIgnoreCase("location"))
                .map(Map.Entry::getValue)
                .findFirst();
        return location;
    }

}
