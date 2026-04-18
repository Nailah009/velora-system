package com.example.orderservice.repository;

import com.example.orderservice.entity.CustomerOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, String> {
}
