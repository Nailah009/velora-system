package com.example.shippingservice.service;

    import com.example.shippingservice.entity.Shipment;
    import com.example.shippingservice.entity.ShipmentStatus;
    import com.example.shippingservice.event.PaymentResultEvent;
    import com.example.shippingservice.event.ShipmentCreatedEvent;
    import com.example.shippingservice.producer.ShipmentProducer;
    import com.example.shippingservice.repository.ShipmentRepository;
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;
    import org.springframework.stereotype.Service;

    import java.time.LocalDateTime;
    import java.util.Optional;
    import java.util.UUID;

    @Service
    public class ShippingService {

        private static final Logger log = LoggerFactory.getLogger(ShippingService.class);

        private final ShipmentRepository shipmentRepository;
        private final ShipmentProducer shipmentProducer;

        public ShippingService(ShipmentRepository shipmentRepository, ShipmentProducer shipmentProducer) {
            this.shipmentRepository = shipmentRepository;
            this.shipmentProducer = shipmentProducer;
        }

        public void createShipment(PaymentResultEvent event) {
            log.info("""
============================================================
SHIPPING SERVICE - CREATE SHIPMENT
============================================================
Order ID      : {}
Recipient     : {}
Courier       : JNE
============================================================
""",
                    event.getOrderId(),
                    event.getCustomerName());

            Shipment shipment = shipmentRepository.findByOrderId(event.getOrderId()).orElseGet(Shipment::new);
            String trackingNumber = shipment.getTrackingNumber();
            if (trackingNumber == null || trackingNumber.isBlank()) {
                trackingNumber = "VLR" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
            }
            if (shipment.getId() == null) {
                shipment.setId(UUID.randomUUID().toString());
                shipment.setOrderId(event.getOrderId());
                shipment.setCreatedAt(LocalDateTime.now());
            }
            shipment.setRecipientName(event.getCustomerName());
            shipment.setRecipientEmail(event.getEmail());
            shipment.setShippingAddress(event.getShippingAddress());
            shipment.setCourier("JNE");
            shipment.setTrackingNumber(trackingNumber);
            shipment.setStatus(ShipmentStatus.READY_TO_SHIP);
            shipmentRepository.save(shipment);

            log.info("""
============================================================
SHIPPING SERVICE - SHIPMENT SAVED
============================================================
Shipment ID   : {}
Order ID      : {}
Recipient     : {}
Courier       : {}
Tracking No   : {}
Status        : {}
============================================================
""",
                    shipment.getId(),
                    shipment.getOrderId(),
                    shipment.getRecipientName(),
                    shipment.getCourier(),
                    shipment.getTrackingNumber(),
                    shipment.getStatus());

            ShipmentCreatedEvent shipmentEvent = new ShipmentCreatedEvent();
            shipmentEvent.setShipmentId(shipment.getId());
            shipmentEvent.setOrderId(shipment.getOrderId());
            shipmentEvent.setCustomerName(shipment.getRecipientName());
            shipmentEvent.setEmail(shipment.getRecipientEmail());
            shipmentEvent.setShippingAddress(shipment.getShippingAddress());
            shipmentEvent.setCourier(shipment.getCourier());
            shipmentEvent.setTrackingNumber(shipment.getTrackingNumber());
            shipmentEvent.setShipmentStatus(shipment.getStatus().name());
            shipmentProducer.sendShipmentCreated(shipmentEvent);
        }

        public Optional<Shipment> findByOrderId(String orderId) {
            return shipmentRepository.findByOrderId(orderId);
        }
    }
