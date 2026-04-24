package com.velora.payment.controller;

import com.velora.payment.common.ApiResponse;
import com.velora.payment.model.Payment;
import com.velora.payment.service.PaymentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) { this.paymentService = paymentService; }

    @GetMapping
    public ApiResponse<List<Payment>> getPayments() {
        return ApiResponse.success(200, "Payments retrieved successfully", paymentService.getAllPayments());
    }

    @GetMapping("/order/{orderId}")
    public ApiResponse<Payment> getPaymentByOrder(@PathVariable Long orderId) {
        return ApiResponse.success(200, "Payment retrieved successfully", paymentService.getLatestByOrderId(orderId));
    }
}
