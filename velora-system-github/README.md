# Velora System

Velora System adalah aplikasi **enterprise fashion e-commerce** berbasis **microservices** yang dibangun menggunakan **Spring Boot**, **Apache Kafka**, dan **MySQL**. Sistem ini dirancang untuk mensimulasikan alur pemrosesan order secara **asynchronous** antar layanan, mulai dari pembuatan order, pembayaran, pengiriman, hingga notifikasi.

Project ini juga telah dilengkapi dengan **OpenAPI documentation** menggunakan **Swagger UI** pada setiap service agar endpoint REST dapat didokumentasikan dan diuji dengan lebih mudah.

---

## Tech Stack

- Java 17+
- Spring Boot 3.x
- Spring Web
- Spring Data JPA
- Apache Kafka
- MySQL 8
- Docker Compose
- Maven
- OpenAPI / Swagger UI

---

## Arsitektur Microservices

Project ini terdiri dari 5 service utama:

### 1. Inventory Service
Berfungsi untuk:
- menampilkan daftar produk
- menampilkan detail varian produk
- melakukan reserve stok saat order dibuat

Port: `8082`

### 2. Order Service
Berfungsi untuk:
- membuat order
- menampilkan detail order
- mengirim event order ke Kafka
- memperbarui status order berdasarkan event dari service lain

Port: `8083`

### 3. Payment Service
Berfungsi untuk:
- menerima event order
- memproses pembayaran
- menyimpan data payment
- mengirim event payment success atau payment failed

Port: `8084`

### 4. Shipping Service
Berfungsi untuk:
- menerima event payment success
- membuat data shipment
- mengirim event shipment created

Port: `8081`

### 5. Notification Service
Berfungsi untuk:
- menerima event payment success
- menerima event payment failed
- menerima event shipment created
- menyimpan log notifikasi

Port: `8085`

---

## Alur Sistem

Alur bisnis utama pada Velora adalah sebagai berikut:

1. User membuat order melalui `order-service`
2. `order-service` memvalidasi produk ke `inventory-service`
3. `inventory-service` melakukan reserve stok
4. `order-service` menyimpan order lalu publish event ke Kafka
5. `payment-service` consume event order dan memproses pembayaran
6. Jika pembayaran berhasil, `payment-service` publish event payment success
7. `shipping-service` consume payment success lalu membuat shipment
8. `shipping-service` publish event shipment created
9. `notification-service` consume event untuk mencatat notifikasi
10. `order-service` memperbarui status order berdasarkan event payment dan shipment

---

## Kafka Event Flow

Project ini menggunakan **Apache Kafka** untuk komunikasi asynchronous antar microservices.

### Topic yang digunakan
- `velora.order.topic`
- `velora.payment.success.topic`
- `velora.payment.failed.topic`
- `velora.shipment.created.topic`

### Dead Letter / Retry Topic
Beberapa service juga menggunakan retry / dead letter topic untuk simulasi failure handling, seperti:
- `payment.order.created.dlq`
- `shipping.payment.success.dlq`

---

## OpenAPI / Swagger UI

Setiap service telah dilengkapi dengan dokumentasi OpenAPI dan dapat diakses melalui Swagger UI.

### Swagger UI
- Inventory Service: `http://localhost:8082/swagger-ui/index.html`
- Order Service: `http://localhost:8083/swagger-ui/index.html`
- Payment Service: `http://localhost:8084/swagger-ui/index.html`
- Shipping Service: `http://localhost:8081/swagger-ui/index.html`
- Notification Service: `http://localhost:8085/swagger-ui/index.html`

### OpenAPI JSON
- Inventory Service: `http://localhost:8082/v3/api-docs`
- Order Service: `http://localhost:8083/v3/api-docs`
- Payment Service: `http://localhost:8084/v3/api-docs`
- Shipping Service: `http://localhost:8081/v3/api-docs`
- Notification Service: `http://localhost:8085/v3/api-docs`

---

## Endpoint Utama

### Inventory Service
- `GET /api/products`
- `GET /api/products/{productId}/variants`
- `GET /api/variants/{sku}`
- `POST /api/inventory/reserve`

### Order Service
- `POST /api/orders`
- `GET /api/orders/{orderId}`

### Payment Service
- `GET /api/payments/order/{orderId}`

### Shipping Service
- `GET /api/shipments/order/{orderId}`

### Notification Service
- `GET /api/notifications/order/{orderId}`

---

## Contoh Testing Flow

### 1. Lihat daftar produk
```http
GET http://localhost:8082/api/products
```

### 2. Buat order
```http
POST http://localhost:8083/api/orders
Content-Type: application/json
```

Body:
```json
{
  "customerName": "Nailah",
  "email": "nailah@gmail.com",
  "shippingAddress": "Jl. Solo Baru No. 5, Kota Solo",
  "paymentMethod": "BCA",
  "items": [
    {
      "sku": "VLR-TSH-BLK-M",
      "quantity": 1
    }
  ]
}
```

### 3. Cek order
```http
GET http://localhost:8083/api/orders/{orderId}
```

### 4. Cek payment
```http
GET http://localhost:8084/api/payments/order/{orderId}
```

### 5. Cek shipment
```http
GET http://localhost:8081/api/shipments/order/{orderId}
```

### 6. Cek notification
```http
GET http://localhost:8085/api/notifications/order/{orderId}
```

---

## Hasil Flow yang Diharapkan

Jika flow berhasil, maka:
- order berhasil dibuat
- payment berhasil diproses
- shipment berhasil dibuat
- notification tersimpan
- status order berubah menjadi `READY_TO_SHIP`

---

## Catatan

- Project ini menggunakan komunikasi **REST + Kafka**
- REST digunakan untuk komunikasi synchronous, misalnya validasi produk dan reserve stok
- Kafka digunakan untuk komunikasi asynchronous antar microservice
- Swagger UI digunakan untuk dokumentasi dan pengujian endpoint REST
- Disarankan menggunakan Java 17 atau versi yang kompatibel

---
