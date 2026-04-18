# RUN VELORA STEP BY STEP

## 1. Nyalakan Docker
Dari root project:
```bash
docker compose up -d
```

Cek container:
```bash
docker ps
```

## 2. Cek RabbitMQ UI
Buka:
- http://localhost:15672

Login:
- username: `guest`
- password: `guest`

## 3. Jalankan semua Spring Boot service di terminal terpisah
```bash
cd shipping-service
mvn spring-boot:run
```

```bash
cd inventory-service
mvn spring-boot:run
```

```bash
cd order-service
mvn spring-boot:run
```

```bash
cd payment-service
mvn spring-boot:run
```

```bash
cd notification-service
mvn spring-boot:run
```

## 4. Urutan demo Postman yang paling aman
1. `GET /api/products`
2. `GET /api/products/{productId}/variants`
3. `GET /api/variants/{sku}`
4. `POST /api/orders`
5. `GET /api/orders/{orderId}`
6. `GET /api/payments/order/{orderId}`
7. `GET /api/shipments/order/{orderId}`
8. `GET /api/notifications/reference/{orderId}`

## 5. Contoh log yang akan terlihat
### order-service
- START CREATE ORDER
- PRODUCT VARIANT VALIDATED
- REQUEST INVENTORY RESERVE
- INVENTORY RESERVE SUCCESS
- ORDER SAVED
- WAITING PAYMENT RESULT
- UPDATE STATUS FROM PAYMENT RESULT
- UPDATE STATUS FROM SHIPMENT RESULT

### inventory-service
- START RESERVE STOCK
- CHECK VARIANT
- STOCK RESERVED
- CONFIRM STOCK AFTER PAYMENT SUCCESS
- RELEASE STOCK AFTER PAYMENT FAILED

### payment-service
- START PROCESS PAYMENT
- PAYMENT SAVED
- RESULT PUBLISHED

### shipping-service
- RECEIVE PAYMENT SUCCESS
- SHIPMENT SAVED
- SHIPMENT EVENT PUBLISHED

### notification-service
- RECEIVE PAYMENT SUCCESS / FAILED
- RECEIVE SHIPMENT CREATED
- NOTIFICATION SAVED
