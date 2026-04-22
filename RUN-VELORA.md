# RUN VELORA

Panduan ini menjelaskan langkah menjalankan Velora System dari nol sampai siap diuji.

---

## 1. Requirement

Pastikan perangkat kamu sudah memiliki:
- Java 17+
- Maven
- Docker Desktop

Cek dengan perintah berikut:

```bash
java -version
mvn -version
docker --version
docker compose version
```

---

## 2. Clone Repository

```bash
git clone https://github.com/Nailah009/velora-system.git
cd velora-system
git checkout velora-system
```

Jika repository sudah ada di laptopmu, cukup pindah ke folder project lalu pastikan branch yang dipakai adalah `velora-system`.

---

## 3. Jalankan Infrastruktur

Dari root project, jalankan:

```bash
docker compose up -d
```

Container yang harus aktif:
- MySQL
- Zookeeper
- Kafka

Cek dengan:

```bash
docker ps
```

---

## 4. Jalankan Semua Service

Buka terminal terpisah untuk masing-masing service.

### Inventory Service
```bash
cd inventory-service
mvn spring-boot:run
```

### Order Service
```bash
cd order-service
mvn spring-boot:run
```

### Payment Service
```bash
cd payment-service
mvn spring-boot:run
```

### Shipping Service
```bash
cd shipping-service
mvn spring-boot:run
```

### Notification Service
```bash
cd notification-service
mvn spring-boot:run
```

Urutan aman menjalankan service:
1. inventory-service
2. order-service
3. payment-service
4. shipping-service
5. notification-service

---

## 5. Cek OpenAPI / Swagger UI

Jika service berhasil berjalan, buka Swagger UI masing-masing service:

- Inventory Service: `http://localhost:8082/swagger-ui/index.html`
- Order Service: `http://localhost:8083/swagger-ui/index.html`
- Payment Service: `http://localhost:8084/swagger-ui/index.html`
- Shipping Service: `http://localhost:8081/swagger-ui/index.html`
- Notification Service: `http://localhost:8085/swagger-ui/index.html`

Cek juga OpenAPI JSON:
- `http://localhost:8082/v3/api-docs`
- `http://localhost:8083/v3/api-docs`
- `http://localhost:8084/v3/api-docs`
- `http://localhost:8081/v3/api-docs`
- `http://localhost:8085/v3/api-docs`

---

## 6. Testing Flow

### A. Cek Produk
Gunakan Swagger UI inventory-service atau browser:

```http
GET http://localhost:8082/api/products
```

### B. Cek Variants
```http
GET http://localhost:8082/api/products/PRD-001/variants
```

### C. Cek Detail SKU
```http
GET http://localhost:8082/api/variants/VLR-TSH-BLK-M
```

### D. Buat Order
Gunakan Swagger UI order-service pada endpoint `POST /api/orders`.

Body contoh:

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

### E. Simpan `orderId`
Setelah order berhasil dibuat, copy `orderId` dari response.

### F. Cek Order
```http
GET http://localhost:8083/api/orders/{orderId}
```

### G. Cek Payment
```http
GET http://localhost:8084/api/payments/order/{orderId}
```

### H. Cek Shipment
```http
GET http://localhost:8081/api/shipments/order/{orderId}
```

### I. Cek Notification
```http
GET http://localhost:8085/api/notifications/order/{orderId}
```

---

## 7. Hasil yang Diharapkan

Jika flow berhasil:
- order berhasil dibuat
- payment status menjadi `SUCCESS`
- shipment berhasil dibuat
- notification untuk payment dan shipment tersimpan
- order status berubah menjadi `READY_TO_SHIP`

---

## 8. Test Skenario Gagal

Untuk simulasi payment gagal, gunakan request order dengan nilai `paymentMethod` yang memicu failure sesuai implementasi service kamu.

Setelah itu cek:
- status order
- data payment
- data shipment
- data notification

---

## 9. Jika Kafka Tidak Terkoneksi

Kalau service gagal connect ke Kafka:
1. cek container Kafka dengan `docker ps`
2. cek log Kafka:
   ```bash
   docker logs velora-kafka
   ```
3. restart infrastruktur:
   ```bash
   docker compose down -v --remove-orphans
   docker compose up -d
   ```

---

## 10. Jika Swagger Tidak Muncul

Pastikan:
- dependency `springdoc-openapi-starter-webmvc-ui` sudah ditambahkan
- versi springdoc sesuai dengan Spring Boot yang dipakai
- service berhasil start tanpa error

---

## 11. Catatan Tambahan

- Jangan push folder `target/` ke GitHub
- Tambahkan `.gitignore` untuk mengabaikan file hasil build
- Disarankan menggunakan branch `velora-system` untuk development
