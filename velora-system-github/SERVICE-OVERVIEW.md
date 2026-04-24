# Velora Service Overview

## 1. shipping-service
Menerima event `payment.success`, membuat data shipment, lalu publish event `shipment.created`.

## 2. inventory-service
Menyimpan data produk dan varian, melakukan reserve stock, confirm stock saat payment sukses, dan release stock saat payment gagal.

## 3. order-service
Pintu masuk utama dari client/Postman.
- validasi request
- cek varian produk ke inventory-service secara REST
- reserve stock ke inventory-service secara REST
- simpan order ke database
- publish event `order.created`
- update status order saat menerima `payment.success`, `payment.failed`, dan `shipment.created`

## 4. payment-service
Consume event `order.created`, simpan payment ke database, lalu publish:
- `payment.success`
- `payment.failed`

## 5. notification-service
Consume event hasil payment dan shipment untuk menyimpan log notifikasi.
