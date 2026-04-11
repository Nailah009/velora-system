INSERT INTO products (id, name, brand, category)
VALUES ('PRD-001', 'Oversize Cotton T-Shirt', 'Velora Essentials', 'T-Shirt')
ON DUPLICATE KEY UPDATE name=VALUES(name), brand=VALUES(brand), category=VALUES(category);

INSERT INTO products (id, name, brand, category)
VALUES ('PRD-002', 'Premium Pullover Hoodie', 'Velora Studio', 'Hoodie')
ON DUPLICATE KEY UPDATE name=VALUES(name), brand=VALUES(brand), category=VALUES(category);

INSERT INTO product_variants (sku, product_id, color_name, size_name, price, available_stock, reserved_stock)
VALUES ('VLR-TSH-BLK-M', 'PRD-001', 'Black', 'M', 129000.00, 20, 0)
ON DUPLICATE KEY UPDATE product_id=VALUES(product_id), color_name=VALUES(color_name), size_name=VALUES(size_name), price=VALUES(price), available_stock=VALUES(available_stock), reserved_stock=VALUES(reserved_stock);

INSERT INTO product_variants (sku, product_id, color_name, size_name, price, available_stock, reserved_stock)
VALUES ('VLR-TSH-WHT-L', 'PRD-001', 'White', 'L', 129000.00, 18, 0)
ON DUPLICATE KEY UPDATE product_id=VALUES(product_id), color_name=VALUES(color_name), size_name=VALUES(size_name), price=VALUES(price), available_stock=VALUES(available_stock), reserved_stock=VALUES(reserved_stock);

INSERT INTO product_variants (sku, product_id, color_name, size_name, price, available_stock, reserved_stock)
VALUES ('VLR-HOD-GRY-L', 'PRD-002', 'Grey', 'L', 249000.00, 15, 0)
ON DUPLICATE KEY UPDATE product_id=VALUES(product_id), color_name=VALUES(color_name), size_name=VALUES(size_name), price=VALUES(price), available_stock=VALUES(available_stock), reserved_stock=VALUES(reserved_stock);

INSERT INTO product_variants (sku, product_id, color_name, size_name, price, available_stock, reserved_stock)
VALUES ('VLR-HOD-NVY-M', 'PRD-002', 'Navy', 'M', 249000.00, 12, 0)
ON DUPLICATE KEY UPDATE product_id=VALUES(product_id), color_name=VALUES(color_name), size_name=VALUES(size_name), price=VALUES(price), available_stock=VALUES(available_stock), reserved_stock=VALUES(reserved_stock);
