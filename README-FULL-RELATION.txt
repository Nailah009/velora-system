VELORA FULL RELATION VERSION

Perubahan utama:
1. Semua service sekarang memakai satu database: velora_db.
2. Relasi database fisik sudah dibuat penuh dengan foreign key:
   - product_variants.product_id -> products.id
   - order_items.order_id -> orders.id
   - order_items.sku -> product_variants.sku
   - payments.order_id -> orders.id
   - shipments.order_id -> orders.id
   - notification_logs.order_id -> orders.id
   - notification_logs.payment_id -> payments.id
   - notification_logs.shipment_id -> shipments.id
3. GET /api/products sekarang flat per item jual, bukan nested varian.

LANGKAH PAKAI:
1. Import file VELORA-FULL-RELATION-SCHEMA.sql ke MySQL.
2. Jalankan ulang inventory-service agar data.sql mengisi data produk dan variant.
3. Jalankan service lain.
4. Tes Postman:
   - GET  http://localhost:8082/api/products
   - POST http://localhost:8083/api/orders
   - GET  http://localhost:8084/api/payments/order/{orderId}
   - GET  http://localhost:8081/api/shipments/order/{orderId}
   - GET  http://localhost:8085/api/notifications/order/{orderId}

Catatan:
Karena environment di sini tidak menyediakan Maven, file ini belum bisa saya compile-test langsung. Perubahannya sudah saya sesuaikan dengan struktur kode proyek kamu dan skema database relasional penuh.
