Patch ini dibuat di atas repo GitHub velora-system yang sudah berisi ORDER DLQ.
Yang diperbarui:
1. shipping-service/config/RabbitConfig.java
2. shipping-service/consumer/PaymentSuccessConsumer.java
3. shipping-service/consumer/PaymentSuccessDlqConsumer.java
4. shipping-service/resources/application.yml

Test SHIPPING DLQ:
POST http://localhost:8083/api/orders

{
  "customerName": "Nai",
  "email": "nai@mail.com",
  "shippingAddress": "FAIL-SHIPPING",
  "paymentMethod": "BCA",
  "items": [
    {
      "sku": "VLR-TSH-BLK-M",
      "quantity": 1
    }
  ]
}

Output yang diharapkan:
- SHIPPING CONSUMER - RECEIVE PAYMENT SUCCESS
- SHIPPING CONSUMER - gagal proses orderId=...
- SHIPPING DLQ CONSUMER
- Pesan masuk Dead Letter Queue
