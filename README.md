# 🛍️ Velora E-Commerce Microservices

Velora adalah simulasi **fashion e-commerce berbasis microservices** untuk Enterprise Application Integration (EAI).
---

## 🧰 Tech Stack

| Kategori | Teknologi |
|---|---|
| Backend | Java 17, Spring Boot |
| API | REST API |
| Auth | Spring Security + JWT Bearer Token |
| Dokumentasi API | OpenAPI / Swagger UI |
| Database | MySQL 8.0 |
| Message Broker | Apache Kafka + Zookeeper |
| ORM | Spring Data JPA / Hibernate |
| Container | Docker Compose |
| Build Tool | Maven |

---

## 🧩 Daftar Service

| Service | Port | Database | Fungsi |
|---|---:|---|---|
| 🔐 auth-service | 8080 | `auth_db` | Register, login, generate JWT, role ADMIN/USER |
| 📦 inventory-service | 8082 | `inventory_db` | Kelola produk, cek stok, reserve stock |
| 🧾 order-service | 8083 | `order_db` | Membuat order, validasi produk, publish order event |
| 💳 payment-service | 8084 | `payment_db` | Consume order event, proses payment, publish payment result, DLQ order |
| 🚚 shipping-service | 8081 | `shipping_db` | Consume payment success, membuat shipment, DLQ shipping |
| 🔔 notification-service | 8085 | `notification_db` | Consume payment/shipment event dan simpan notifikasi |

---

## 🗄️ Database Per Service

Project ini **bukan shared database**. Semua service memakai satu MySQL container, tetapi schema/database-nya berbeda:

```text
auth-service          → auth_db
inventory-service     → inventory_db
order-service         → order_db
payment-service       → payment_db
shipping-service      → shipping_db
notification-service  → notification_db
```

Relasi antarservice tidak dibuat menggunakan foreign key lintas database. Service saling terhubung menggunakan **REST API** dan **Kafka event**.

---

## 🧠 Konsep Integrasi

### 1. REST Synchronous

Digunakan saat order-service perlu validasi dan reserve stock ke inventory-service.

```text
order-service → inventory-service
```

### 2. Kafka Asynchronous

Digunakan untuk proses lanjutan setelah order dibuat.

```text
order-service
  ↓ publish velora.order.topic
payment-service
  ↓ publish velora.payment.success.topic / velora.payment.failed.topic
shipping-service
  ↓ publish velora.shipment.created.topic
notification-service
```

### 3. DLQ / Dead Letter Topic

Digunakan untuk kegagalan teknis saat consumer gagal memproses event.

```text
paymentMethod = DLQ
→ payment consumer gagal
→ event masuk velora.order.topic.DLQ
→ Payment DLQ Consumer menampilkan log
```

```text
shippingAddress = FAIL-SHIPPING
→ shipping consumer gagal
→ event masuk velora.payment.success.topic.DLQ
→ Shipping DLQ Consumer menampilkan log
```

---

## 🐳 Menjalankan Infrastruktur

Masuk ke root project:

```powershell
cd D:\velora-system-eai\velora_system
```

Jalankan Docker Compose:

```powershell
docker compose up -d
```

Cek container:

```powershell
docker ps
```

Harus muncul:

```text
velora-mysql       0.0.0.0:3307->3306/tcp
velora-kafka       0.0.0.0:9092->9092/tcp
velora-zookeeper   0.0.0.0:2181->2181/tcp
```

Kalau container lama bentrok:

```powershell
docker rm -f velora-mysql velora-kafka velora-zookeeper
docker compose up -d
```

Kalau ingin reset database bersih:

```powershell
docker compose down -v
docker compose up -d
```

---

## 🗃️ Cek Database

Masuk MySQL Docker:

```powershell
docker exec -it velora-mysql mysql -u root
```

Cek database:

```sql
SHOW DATABASES;
```

Cek tabel:

```sql
USE auth_db;
SHOW TABLES;

USE inventory_db;
SHOW TABLES;

USE order_db;
SHOW TABLES;

USE payment_db;
SHOW TABLES;

USE shipping_db;
SHOW TABLES;

USE notification_db;
SHOW TABLES;
```

Expected:

```text
auth_db          → users
inventory_db     → product_variants
order_db         → orders, order_items
payment_db       → payments
shipping_db      → shipments
notification_db  → notification_logs
```

---

## ▶️ Menjalankan Service Spring Boot

Buka terminal berbeda untuk masing-masing service.

### 🔐 Auth Service

```powershell
cd D:\velora-system-eai\velora_system\auth-service
mvn spring-boot:run
```

### 📦 Inventory Service

```powershell
cd D:\velora-system-eai\velora_system\inventory-service
mvn spring-boot:run
```

### 🧾 Order Service

```powershell
cd D:\velora-system-eai\velora_system\order-service
mvn spring-boot:run
```

### 💳 Payment Service

```powershell
cd D:\velora-system-eai\velora_system\payment-service
mvn spring-boot:run
```

### 🚚 Shipping Service

```powershell
cd D:\velora-system-eai\velora_system\shipping-service
mvn spring-boot:run
```

### 🔔 Notification Service

```powershell
cd D:\velora-system-eai\velora_system\notification-service
mvn spring-boot:run
```

---

## 📘 Swagger UI

| Service | Swagger URL |
|---|---|
| 🔐 Auth | http://localhost:8080/swagger-ui.html |
| 📦 Inventory | http://localhost:8082/swagger-ui.html |
| 🧾 Order | http://localhost:8083/swagger-ui.html |
| 💳 Payment | http://localhost:8084/swagger-ui.html |
| 🚚 Shipping | http://localhost:8081/swagger-ui.html |
| 🔔 Notification | http://localhost:8085/swagger-ui.html |

> Catatan: jika memakai tombol **Authorize** di Swagger, biasanya cukup paste **token saja tanpa kata `Bearer`**. Jika memakai kolom manual `Authorization`, gunakan format `Bearer <token>`.

---

## 👤 Akun Default

| Role | Username | Password | Keterangan |
|---|---|---|---|
| ADMIN | `admin` | `admin123` | Akun admin otomatis dari seeder |
| USER | `user` | `user123` | Akun user default |

User baru yang register otomatis mendapat role `USER`.

---

## 👑 Flow Testing ADMIN

### 1. Login Admin

Swagger Auth:

```http
POST /api/auth/login
```

Body:

```json
{
  "username": "admin",
  "password": "admin123"
}
```

Copy token dari response.

### 2. Authorize di Inventory Swagger

Buka:

```text
http://localhost:8082/swagger-ui.html
```

Klik **Authorize**, paste token admin.

### 3. Cek Produk

```http
GET /api/products
```

Produk seed sudah banyak, misalnya:

```text
VLR-TSH-BLK-M
VLR-HOODIE-BLK-L
VLR-JKT-DNM-L
VLR-PNT-KHK-32
VLR-SHOES-WHT-42
```

### 4. Admin Tambah Produk

```http
POST /api/products
```

Body:

```json
{
  "sku": "VLR-BAG-BLK-01",
  "productName": "Velora Daily Bag Black",
  "category": "Bag",
  "color": "Black",
  "size": "One Size",
  "price": 199000,
  "stock": 25
}
```

Expected:

```json
{
  "status": "success",
  "code": 200,
  "message": "Product created successfully"
}
```

### 5. Admin Test Validasi Gagal

```http
POST /api/products
```

Body invalid:

```json
{
  "sku": "",
  "productName": "",
  "category": "",
  "color": "",
  "size": "",
  "price": -100,
  "stock": -1
}
```

Expected:

```json
{
  "status": "error",
  "code": 400,
  "message": "Validation failed",
  "errors": [
    {
      "field": "sku",
      "message": "SKU is required"
    },
    {
      "field": "productName",
      "message": "Product name is required"
    },
    {
      "field": "price",
      "message": "Price must be positive"
    }
  ]
}
```

---

## 🧑‍💻 Flow Testing USER Sukses

### 1. Register User

Swagger Auth:

```http
POST /api/auth/register
```

Body:

```json
{
  "username": "nailah_user",
  "email": "nailah_user@gmail.com",
  "password": "nailah123"
}
```

Kalau sudah ada, ganti username menjadi `nailah_user2`.

### 2. Login User

```http
POST /api/auth/login
```

Body:

```json
{
  "username": "nailah_user",
  "password": "nailah123"
}
```

Copy token USER.

### 3. USER Tidak Boleh Tambah Produk

Di Inventory Swagger, pakai token USER lalu coba:

```http
POST /api/products
```

Expected:

```text
403 Forbidden
```

Ini membuktikan role authorization jalan.

### 4. USER Lihat Produk

```http
GET /api/products
```

Catat SKU, misalnya:

```text
VLR-TSH-BLK-M
```

### 5. USER Buat Order

Swagger Order:

```http
POST /api/orders
```

Body:

```json
{
  "shippingAddress": "Jl. Veteran No. 10, Yogyakarta",
  "paymentMethod": "BCA",
  "items": [
    {
      "sku": "VLR-TSH-BLK-M",
      "quantity": 1
    }
  ]
}
```

Expected:

```json
{
  "status": "success",
  "code": 201,
  "message": "Order created successfully",
  "data": {
    "order_id": "ORD-2026-0001",
    "internal_id": 1,
    "status": "PENDING_PAYMENT"
  }
}
```

Gunakan `internal_id` untuk cek payment, shipping, notification.

### 6. Cek Hasil Otomatis

```http
GET http://localhost:8083/api/orders/{internal_id}
GET http://localhost:8084/api/payments/order/{internal_id}
GET http://localhost:8081/api/shipments/order/{internal_id}
GET http://localhost:8085/api/notifications/order/{internal_id}
```

Expected:

```text
Payment      → PAID
Shipping     → SHIPPED
Notification → PAYMENT_SUCCESS dan SHIPMENT_CREATED
```

---

## ✅ Console Log Sukses yang Diharapkan

Saat order sukses, terminal akan menampilkan log rapi:

```text
============================================================
ORDER SERVICE - START CREATE ORDER
============================================================
Customer       : nailah_user
Email          : nailah_user@gmail.com
Payment Method : BCA
Items          : [VLR-TSH-BLK-M x1]
============================================================
```

```text
============================================================
INVENTORY SERVICE - STOCK RESERVED
============================================================
SKU           : VLR-TSH-BLK-M
New Available : 49
New Reserved  : 1
============================================================
```

```text
============================================================
PAYMENT CONSUMER - RECEIVE ORDER CREATED
============================================================
Queue          : velora.order.topic
Order ID       : 1
Customer       : nailah_user
Payment Method : BCA
============================================================
```

```text
============================================================
SHIPPING SERVICE - CREATE SHIPMENT
============================================================
Order ID       : 1
Courier        : JNE
Status         : SHIPPED
============================================================
```

```text
============================================================
NOTIFICATION SERVICE - NOTIFICATION SAVED
============================================================
Type           : SHIPMENT_CREATED
Status         : SENT
============================================================
```

---

## ❌ Flow Gagal 1: USER Tidak Boleh Tambah Produk

Pakai token USER:

```http
POST /api/products
```

Expected:

```text
403 Forbidden
```

Penjelasan:

> USER hanya boleh melihat produk dan membuat order. Endpoint tambah produk hanya untuk ADMIN.

---

## ❌ Flow Gagal 2: Validasi Order Gagal

Pakai token USER di order-service:

```http
POST /api/orders
```

Body:

```json
{
  "shippingAddress": "",
  "paymentMethod": "",
  "items": [
    {
      "sku": "",
      "quantity": 0
    }
  ]
}
```

Expected:

```json
{
  "status": "error",
  "code": 400,
  "message": "Validation failed",
  "errors": [
    {
      "field": "shippingAddress",
      "message": "Shipping address is required"
    },
    {
      "field": "items[0].quantity",
      "message": "Quantity must be positive"
    }
  ]
}
```

---

## ❌ Flow Gagal 3: Stok Tidak Cukup

```json
{
  "shippingAddress": "Jl. Veteran No. 10, Yogyakarta",
  "paymentMethod": "BCA",
  "items": [
    {
      "sku": "VLR-TSH-BLK-M",
      "quantity": 9999
    }
  ]
}
```

Expected:

```text
Stock is not enough / Insufficient stock
```

---

## ❌ Flow Gagal 4: Payment Business Failed

Gunakan `paymentMethod` = `FAIL`.

```json
{
  "shippingAddress": "Jl. Veteran No. 10, Yogyakarta",
  "paymentMethod": "FAIL",
  "items": [
    {
      "sku": "VLR-TSH-BLK-M",
      "quantity": 1
    }
  ]
}
```

Expected:

```text
Payment      → FAILED
Order Status → PAYMENT_FAILED
Shipping     → tidak dibuat
Notification → PAYMENT_FAILED
```

Ini adalah **business failure**, bukan DLQ.

---

## ☠️ Flow DLQ 1: Payment Consumer Technical Failed

Gunakan `paymentMethod` = `DLQ`.

```json
{
  "shippingAddress": "Jl. Veteran No. 10, Yogyakarta",
  "paymentMethod": "DLQ",
  "items": [
    {
      "sku": "VLR-TSH-BLK-M",
      "quantity": 1
    }
  ]
}
```

Expected di terminal payment-service:

```text
============================================================
PAYMENT CONSUMER - RECEIVE ORDER CREATED
============================================================
Queue          : velora.order.topic
Order ID       : 3
Customer       : nailah_user
Payment Method : DLQ
============================================================

============================================================
PAYMENT CONSUMER - GAGAL PROSES ORDER
============================================================
Order ID       : 3
Reason         : Forced technical failure for DLQ testing
Action         : Throw exception, retry, then send to DLQ
============================================================

============================================================
PAYMENT DLQ CONSUMER
============================================================
Pesan masuk Dead Letter Queue
Queue          : velora.order.topic.DLQ
Order ID       : 3
Customer       : nailah_user
Payment Method : DLQ
Reason         : Payment consumer failed after retry
Body           : {...}
============================================================
```

Cek topic:

```powershell
docker exec -it velora-kafka kafka-topics --bootstrap-server localhost:9092 --list
```

Harus ada:

```text
velora.order.topic.DLQ
```

---

## ☠️ Flow DLQ 2: Shipping Consumer Technical Failed

Gunakan `shippingAddress` = `FAIL-SHIPPING`, payment method tetap normal.

```json
{
  "shippingAddress": "FAIL-SHIPPING",
  "paymentMethod": "BCA",
  "items": [
    {
      "sku": "VLR-TSH-BLK-M",
      "quantity": 1
    }
  ]
}
```

Expected di terminal shipping-service:

```text
============================================================
SHIPPING CONSUMER - RECEIVE PAYMENT SUCCESS
============================================================
Queue          : velora.payment.success.topic
Order ID       : 4
Customer       : nailah_user
Payment Status : PAID
Address        : FAIL-SHIPPING
============================================================

============================================================
SHIPPING CONSUMER - GAGAL PROSES SHIPMENT
============================================================
Order ID       : 4
Reason         : Forced shipping failure for DLQ testing
Action         : Throw exception, retry, then send to DLQ
============================================================

============================================================
SHIPPING DLQ CONSUMER
============================================================
Pesan masuk Dead Letter Queue
Queue          : velora.payment.success.topic.DLQ
Order ID       : 4
Customer       : nailah_user
Payment Status : PAID
Address        : FAIL-SHIPPING
Reason         : Shipping consumer failed after retry
Body           : {...}
============================================================
```

Cek topic:

```powershell
docker exec -it velora-kafka kafka-topics --bootstrap-server localhost:9092 --list
```

Harus ada:

```text
velora.payment.success.topic.DLQ
```

---

## 📌 Perbedaan FAIL dan DLQ

| Skenario | Jenis Gagal | Hasil |
|---|---|---|
| `paymentMethod: "FAIL"` | Business failure | Payment dibuat dengan status FAILED, notification PAYMENT_FAILED |
| `paymentMethod: "DLQ"` | Technical failure | Payment consumer error, message masuk `velora.order.topic.DLQ` |
| `shippingAddress: "FAIL-SHIPPING"` | Technical failure | Shipping consumer error, message masuk `velora.payment.success.topic.DLQ` |

---

## 📸 Bukti Screenshot yang Disarankan

1. `docker ps` menampilkan `velora-mysql`, `velora-kafka`, `velora-zookeeper`.
2. Swagger login admin mendapat JWT.
3. Swagger admin tambah produk berhasil.
4. Swagger user register dan login berhasil.
5. USER gagal tambah produk `403 Forbidden`.
6. USER create order sukses dengan `order_id: ORD-2026-xxxx`.
7. Swagger payment menampilkan status `PAID`.
8. Swagger shipping menampilkan status `SHIPPED`.
9. Swagger notification menampilkan notifikasi.
10. Terminal sukses: order, inventory, payment, shipping, notification.
11. Validasi gagal: response `errors[]`.
12. Payment failed business: `paymentMethod: FAIL`.
13. Payment DLQ: `paymentMethod: DLQ`.
14. Shipping DLQ: `shippingAddress: FAIL-SHIPPING`.
15. SQL `SHOW TABLES` untuk membuktikan database per service.

---

## 🗣️ Script Penjelasan Singkat Demo

> Velora menggunakan arsitektur microservices dengan database terpisah per service. Auth-service menghasilkan JWT untuk autentikasi dan role authorization. Inventory-service mengelola produk dan stok. Order-service membuat order dan melakukan reserve stock melalui REST ke inventory-service. Setelah order dibuat, order-service mengirim event ke Kafka. Payment-service, shipping-service, dan notification-service memproses event secara asynchronous. Sistem juga menerapkan DLQ untuk menangani kegagalan teknis pada consumer, sehingga message gagal tidak hilang dan dapat dianalisis melalui topic DLQ.

