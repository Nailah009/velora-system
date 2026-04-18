# Velora - Enterprise Fashion Order Fulfillment System

Project ini adalah versi pengembangan dari arsitektur lama cinema ticket booking yang sudah diubah domain bisnisnya menjadi **enterprise fashion e-commerce**. Pola implementasi tetap sama:
- banyak project Spring Boot, bukan satu project jadi satu
- setiap service punya database sendiri
- RabbitMQ dipakai untuk komunikasi asynchronous
- request demo dilakukan lewat Postman
- hasil request terlihat di terminal console log

## Daftar service
1. `shipping-service` (port **8081**)
2. `inventory-service` (port **8082**)
3. `order-service` (port **8083**)
4. `payment-service` (port **8084**)
5. `notification-service` (port **8085**)

## Arsitektur singkat
### Synchronous REST
- `order-service` memanggil `inventory-service` untuk cek varian produk
- `order-service` memanggil `inventory-service` untuk reserve stock

### Asynchronous RabbitMQ
- `order-service` publish event `order.created`
- `payment-service` consume `order.created`
- `payment-service` publish `payment.success` / `payment.failed`
- `inventory-service`, `order-service`, `shipping-service`, dan `notification-service` consume hasil payment
- `shipping-service` publish `shipment.created`
- `order-service` dan `notification-service` consume `shipment.created`

## Infrastruktur
Folder ini juga menyediakan:
- `docker-compose.yml`
- `mysql-init/01-create-dbs.sql`
- `postman/Velora-EAI.postman_collection.json`
- `RUN-VELORA.md`

## Requirement
- Java 17
- Maven
- Docker Desktop

## 1. Jalankan RabbitMQ + MySQL
Dari root folder:
```bash
docker compose up -d
```

RabbitMQ UI:
- http://localhost:15672
- username: `guest`
- password: `guest`

MySQL:
- host: `localhost`
- port: `3306`
- user: `root`
- password: `root123`

## 2. Jalankan semua service
Buka terminal terpisah untuk masing-masing service:

### shipping-service
```bash
cd shipping-service
mvn spring-boot:run
```

### inventory-service
```bash
cd inventory-service
mvn spring-boot:run
```

### order-service
```bash
cd order-service
mvn spring-boot:run
```

### payment-service
```bash
cd payment-service
mvn spring-boot:run
```

### notification-service
```bash
cd notification-service
mvn spring-boot:run
```

## 3. Flow testing via Postman
### A. Lihat produk
`GET http://localhost:8082/api/products`

### B. Lihat varian produk
`GET http://localhost:8082/api/products/PRD-001/variants`

### C. Detail satu SKU
`GET http://localhost:8082/api/variants/VLR-TSH-BLK-M`

### D. Buat order sukses
`POST http://localhost:8083/api/orders`

Body:
```json
{
  "customerName": "Selvya",
  "email": "selvya@mail.com",
  "shippingAddress": "Jl. Solo Baru No. 21, Sukoharjo",
  "paymentMethod": "QRIS",
  "items": [
    {
      "sku": "VLR-TSH-BLK-M",
      "quantity": 2
    },
    {
      "sku": "VLR-HOD-GRY-L",
      "quantity": 1
    }
  ]
}
```

### E. Cek status order
`GET http://localhost:8083/api/orders/{orderId}`

### F. Cek data payment
`GET http://localhost:8084/api/payments/order/{orderId}`

### G. Cek shipment
`GET http://localhost:8081/api/shipments/order/{orderId}`

### H. Cek notifikasi
`GET http://localhost:8085/api/notifications/reference/{orderId}`

## Testing payment gagal
Gunakan body ini:
```json
{
  "customerName": "Dimas",
  "email": "dimas@mail.com",
  "shippingAddress": "Jl. Veteran No. 8, Surakarta",
  "paymentMethod": "FAIL",
  "items": [
    {
      "sku": "VLR-TSH-WHT-L",
      "quantity": 1
    }
  ]
}
```

Hasil:
- payment akan gagal
- order status jadi `PAYMENT_FAILED`
- stok yang sudah di-reserve akan di-release oleh `inventory-service`
- tidak ada shipment dibuat
- notification akan mencatat pesan gagal

## Database yang dipakai
- `shipping_db`
- `inventory_db`
- `order_db`
- `payment_db`
- `notification_db`

## Catatan penting
- `inventory-service` di-seed otomatis lewat `data.sql`
- `order-service` adalah pintu masuk utama untuk demo di Postman
- `paymentMethod = FAIL` dipakai untuk simulasi gagal saat demo
