package com.example.shippingservice.repository;

import com.example.shippingservice.entity.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShipmentRepository extends JpaRepository<Shipment, String> {
    List<Shipment> findByOrderId(String orderId);
}
