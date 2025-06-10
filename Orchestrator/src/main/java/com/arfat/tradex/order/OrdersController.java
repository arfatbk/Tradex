package com.arfat.tradex.order;

import com.arfat.tradex.order.model.Order;
import com.arfat.tradex.order.model.OrderRequest;
import com.arfat.tradex.order.model.OrderResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/orders")
public class OrdersController {

    private final OrderService orderService;

    public OrdersController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> placeOrder(@Validated @RequestBody OrderRequest orderRequest) {
        Order order = orderService.placeOrder(OrderRequest.toOrder(orderRequest));
        URI location = URI.create(order.getId());
        return ResponseEntity.created(location).build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable String id) {
        var order = orderService.getOrder(id);
        return ResponseEntity.ok(OrderResponse.from(order));

    }

}
