Velora full relation + pretty console log + DLQ

Perubahan utama:
- Satu database: velora_db
- Full foreign key antar tabel inti
- Endpoint GET /api/products flat per item jual
- Pretty console log di order, inventory, payment, shipping, notification
- Payment DLQ dan Shipping DLQ aktif

Trigger uji:
1. Normal: paymentMethod=BCA dan shippingAddress normal
2. Payment DLQ: paymentMethod=DLQ
3. Shipping DLQ: shippingAddress=FAIL-SHIPPING
