package com.velora.payment.repository;

import com.velora.payment.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findFirstByOrderIdOrderByIdDesc(Long orderId);
    List<Payment> findByOrderId(Long orderId);
}
