package com.example.orderservice.consumer;

import com.example.orderservice.config.RabbitConfig;
import com.example.orderservice.event.PaymentResultEvent;
import com.example.orderservice.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentResultConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentResultConsumer.class);

    private final OrderService orderService;

    public PaymentResultConsumer(OrderService orderService) {
        this.orderService = orderService;
    }

    @RabbitListener(queues = RabbitConfig.ORDER_PAYMENT_SUCCESS_QUEUE)
    public void handlePaymentSuccess(PaymentResultEvent event) {
        log.info("""
============================================================
ORDER CONSUMER - RECEIVE PAYMENT SUCCESS
============================================================
Queue         : {}
Order ID      : {}
Customer      : {}
Payment Status: {}
Amount        : {}
============================================================
""",
                RabbitConfig.ORDER_PAYMENT_SUCCESS_QUEUE,
                event.getOrderId(),
                event.getCustomerName(),
                event.getPaymentStatus(),
                event.getAmount());
        orderService.updateFromPaymentResult(event);
    }

    @RabbitListener(queues = RabbitConfig.ORDER_PAYMENT_FAILED_QUEUE)
    public void handlePaymentFailed(PaymentResultEvent event) {
        log.info("""
============================================================
ORDER CONSUMER - RECEIVE PAYMENT FAILED
============================================================
Queue         : {}
Order ID      : {}
Customer      : {}
Payment Status: {}
Reason        : {}
============================================================
""",
                RabbitConfig.ORDER_PAYMENT_FAILED_QUEUE,
                event.getOrderId(),
                event.getCustomerName(),
                event.getPaymentStatus(),
                event.getReason());
        orderService.updateFromPaymentResult(event);
    }
}
