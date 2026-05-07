package com.velora.shipping.repository;

import com.velora.shipping.model.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
    Optional<Shipment> findFirstByOrderIdOrderByIdDesc(Long orderId);
}
