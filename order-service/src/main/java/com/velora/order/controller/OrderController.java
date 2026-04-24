package com.velora.order.controller;

import com.velora.order.common.ApiResponse;
import com.velora.order.dto.CreateOrderRequest;
import com.velora.order.dto.OrderResponse;
import com.velora.order.model.Order;
import com.velora.order.security.JwtUser;
import com.velora.order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(@Valid @RequestBody CreateOrderRequest request,
                                                                  Authentication authentication,
                                                                  @RequestHeader("Authorization") String authorizationHeader) throws Exception {
        JwtUser user = (JwtUser) authentication.getPrincipal();
        Order order = orderService.createOrder(request, user, authorizationHeader);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(201, "Order created successfully", OrderResponse.from(order)));
    }

    @GetMapping("/{orderId}")
    public ApiResponse<OrderResponse> getOrder(@PathVariable Long orderId) {
        return ApiResponse.success(200, "Order retrieved successfully", OrderResponse.from(orderService.getOrder(orderId)));
    }

    @GetMapping
    public ApiResponse<List<OrderResponse>> getOrders() {
        return ApiResponse.success(200, "Orders retrieved successfully", orderService.getAllOrders().stream().map(OrderResponse::from).toList());
    }
}
