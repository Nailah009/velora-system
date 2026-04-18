USE velora_db;

CREATE TABLE IF NOT EXISTS products (
    id VARCHAR(100) PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    brand VARCHAR(150) NOT NULL,
    category VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS product_variants (
    sku VARCHAR(100) PRIMARY KEY,
    product_id VARCHAR(100) NOT NULL,
    color_name VARCHAR(100) NOT NULL,
    size_name VARCHAR(50) NOT NULL,
    price DECIMAL(15,2) NOT NULL,
    available_stock INT NOT NULL DEFAULT 0,
    reserved_stock INT NOT NULL DEFAULT 0,
    CONSTRAINT fk_product_variants_product
        FOREIGN KEY (product_id) REFERENCES products(id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    INDEX idx_product_variants_product (product_id)
);

CREATE TABLE IF NOT EXISTS orders (
    id VARCHAR(100) PRIMARY KEY,
    customer_name VARCHAR(150) NOT NULL,
    email VARCHAR(200) NOT NULL,
    shipping_address VARCHAR(1000) NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    total_amount DECIMAL(15,2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS order_items (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    order_id VARCHAR(100) NOT NULL,
    sku VARCHAR(100) NOT NULL,
    product_name VARCHAR(200) NOT NULL,
    color_name VARCHAR(100) NULL,
    size_name VARCHAR(50) NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(15,2) NOT NULL,
    subtotal DECIMAL(15,2) NOT NULL,
    CONSTRAINT fk_order_items_order
        FOREIGN KEY (order_id) REFERENCES orders(id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_order_items_variant
        FOREIGN KEY (sku) REFERENCES product_variants(sku)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    INDEX idx_order_items_order (order_id),
    INDEX idx_order_items_sku (sku)
);

CREATE TABLE IF NOT EXISTS payments (
    id VARCHAR(100) PRIMARY KEY,
    order_id VARCHAR(100) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    reason VARCHAR(500) NULL,
    created_at DATETIME NOT NULL,
    CONSTRAINT uk_payments_order UNIQUE (order_id),
    CONSTRAINT fk_payments_order
        FOREIGN KEY (order_id) REFERENCES orders(id)
        ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS shipments (
    id VARCHAR(100) PRIMARY KEY,
    order_id VARCHAR(100) NOT NULL,
    recipient_name VARCHAR(150) NOT NULL,
    recipient_email VARCHAR(200) NOT NULL,
    shipping_address VARCHAR(1000) NOT NULL,
    courier VARCHAR(100) NOT NULL,
    tracking_number VARCHAR(150) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at DATETIME NOT NULL,
    CONSTRAINT uk_shipments_order UNIQUE (order_id),
    CONSTRAINT uk_shipments_tracking UNIQUE (tracking_number),
    CONSTRAINT fk_shipments_order
        FOREIGN KEY (order_id) REFERENCES orders(id)
        ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS notification_logs (
    id VARCHAR(100) PRIMARY KEY,
    order_id VARCHAR(100) NOT NULL,
    payment_id VARCHAR(100) NULL,
    shipment_id VARCHAR(100) NULL,
    recipient VARCHAR(200) NOT NULL,
    type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    message VARCHAR(1000) NOT NULL,
    created_at DATETIME NOT NULL,
    CONSTRAINT fk_notification_order
        FOREIGN KEY (order_id) REFERENCES orders(id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_notification_payment
        FOREIGN KEY (payment_id) REFERENCES payments(id)
        ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT fk_notification_shipment
        FOREIGN KEY (shipment_id) REFERENCES shipments(id)
        ON UPDATE CASCADE ON DELETE SET NULL,
    INDEX idx_notification_order (order_id),
    INDEX idx_notification_payment (payment_id),
    INDEX idx_notification_shipment (shipment_id)
);
