INSERT INTO products (id, name, brand, category)
VALUES ('PRD-001', 'Oversize Cotton T-Shirt', 'Velora Essentials', 'T-Shirt')
ON DUPLICATE KEY UPDATE name=VALUES(name), brand=VALUES(brand), category=VALUES(category);

INSERT INTO products (id, name, brand, category)
VALUES ('PRD-002', 'Premium Pullover Hoodie', 'Velora Studio', 'Hoodie')
ON DUPLICATE KEY UPDATE name=VALUES(name), brand=VALUES(brand), category=VALUES(category);

INSERT INTO products (id, name, brand, category)
VALUES ('PRD-003', 'Utility Cargo Pants', 'Velora Utility', 'Pants')
ON DUPLICATE KEY UPDATE name=VALUES(name), brand=VALUES(brand), category=VALUES(category);

INSERT INTO products (id, name, brand, category)
VALUES ('PRD-004', 'Relaxed Linen Shirt', 'Velora Weekend', 'Shirt')
ON DUPLICATE KEY UPDATE name=VALUES(name), brand=VALUES(brand), category=VALUES(category);

INSERT INTO products (id, name, brand, category)
VALUES ('PRD-005', 'Pleated Tennis Skirt', 'Velora Motion', 'Skirt')
ON DUPLICATE KEY UPDATE name=VALUES(name), brand=VALUES(brand), category=VALUES(category);

INSERT INTO products (id, name, brand, category)
VALUES ('PRD-006', 'Cropped Bomber Jacket', 'Velora Studio', 'Jacket')
ON DUPLICATE KEY UPDATE name=VALUES(name), brand=VALUES(brand), category=VALUES(category);

INSERT INTO product_variants (sku, product_id, color_name, size_name, price, available_stock, reserved_stock)
VALUES ('VLR-TSH-BLK-M', 'PRD-001', 'Black', 'M', 129000.00, 20, 0)
ON DUPLICATE KEY UPDATE product_id=VALUES(product_id), color_name=VALUES(color_name), size_name=VALUES(size_name), price=VALUES(price), available_stock=VALUES(available_stock), reserved_stock=VALUES(reserved_stock);

INSERT INTO product_variants (sku, product_id, color_name, size_name, price, available_stock, reserved_stock)
VALUES ('VLR-TSH-WHT-L', 'PRD-001', 'White', 'L', 129000.00, 18, 0)
ON DUPLICATE KEY UPDATE product_id=VALUES(product_id), color_name=VALUES(color_name), size_name=VALUES(size_name), price=VALUES(price), available_stock=VALUES(available_stock), reserved_stock=VALUES(reserved_stock);

INSERT INTO product_variants (sku, product_id, color_name, size_name, price, available_stock, reserved_stock)
VALUES ('VLR-TSH-SGE-S', 'PRD-001', 'Sage', 'S', 135000.00, 14, 0)
ON DUPLICATE KEY UPDATE product_id=VALUES(product_id), color_name=VALUES(color_name), size_name=VALUES(size_name), price=VALUES(price), available_stock=VALUES(available_stock), reserved_stock=VALUES(reserved_stock);

INSERT INTO product_variants (sku, product_id, color_name, size_name, price, available_stock, reserved_stock)
VALUES ('VLR-HOD-GRY-L', 'PRD-002', 'Grey', 'L', 249000.00, 15, 0)
ON DUPLICATE KEY UPDATE product_id=VALUES(product_id), color_name=VALUES(color_name), size_name=VALUES(size_name), price=VALUES(price), available_stock=VALUES(available_stock), reserved_stock=VALUES(reserved_stock);

INSERT INTO product_variants (sku, product_id, color_name, size_name, price, available_stock, reserved_stock)
VALUES ('VLR-HOD-NVY-M', 'PRD-002', 'Navy', 'M', 249000.00, 12, 0)
ON DUPLICATE KEY UPDATE product_id=VALUES(product_id), color_name=VALUES(color_name), size_name=VALUES(size_name), price=VALUES(price), available_stock=VALUES(available_stock), reserved_stock=VALUES(reserved_stock);

INSERT INTO product_variants (sku, product_id, color_name, size_name, price, available_stock, reserved_stock)
VALUES ('VLR-HOD-BRN-XL', 'PRD-002', 'Brown', 'XL', 259000.00, 9, 0)
ON DUPLICATE KEY UPDATE product_id=VALUES(product_id), color_name=VALUES(color_name), size_name=VALUES(size_name), price=VALUES(price), available_stock=VALUES(available_stock), reserved_stock=VALUES(reserved_stock);

INSERT INTO product_variants (sku, product_id, color_name, size_name, price, available_stock, reserved_stock)
VALUES ('VLR-CGO-KHK-M', 'PRD-003', 'Khaki', 'M', 279000.00, 10, 0)
ON DUPLICATE KEY UPDATE product_id=VALUES(product_id), color_name=VALUES(color_name), size_name=VALUES(size_name), price=VALUES(price), available_stock=VALUES(available_stock), reserved_stock=VALUES(reserved_stock);

INSERT INTO product_variants (sku, product_id, color_name, size_name, price, available_stock, reserved_stock)
VALUES ('VLR-CGO-BLK-L', 'PRD-003', 'Black', 'L', 279000.00, 8, 0)
ON DUPLICATE KEY UPDATE product_id=VALUES(product_id), color_name=VALUES(color_name), size_name=VALUES(size_name), price=VALUES(price), available_stock=VALUES(available_stock), reserved_stock=VALUES(reserved_stock);

INSERT INTO product_variants (sku, product_id, color_name, size_name, price, available_stock, reserved_stock)
VALUES ('VLR-LIN-BLU-M', 'PRD-004', 'Sky Blue', 'M', 189000.00, 11, 0)
ON DUPLICATE KEY UPDATE product_id=VALUES(product_id), color_name=VALUES(color_name), size_name=VALUES(size_name), price=VALUES(price), available_stock=VALUES(available_stock), reserved_stock=VALUES(reserved_stock);

INSERT INTO product_variants (sku, product_id, color_name, size_name, price, available_stock, reserved_stock)
VALUES ('VLR-LIN-CRM-L', 'PRD-004', 'Cream', 'L', 189000.00, 13, 0)
ON DUPLICATE KEY UPDATE product_id=VALUES(product_id), color_name=VALUES(color_name), size_name=VALUES(size_name), price=VALUES(price), available_stock=VALUES(available_stock), reserved_stock=VALUES(reserved_stock);

INSERT INTO product_variants (sku, product_id, color_name, size_name, price, available_stock, reserved_stock)
VALUES ('VLR-SKT-WHT-S', 'PRD-005', 'White', 'S', 219000.00, 7, 0)
ON DUPLICATE KEY UPDATE product_id=VALUES(product_id), color_name=VALUES(color_name), size_name=VALUES(size_name), price=VALUES(price), available_stock=VALUES(available_stock), reserved_stock=VALUES(reserved_stock);

INSERT INTO product_variants (sku, product_id, color_name, size_name, price, available_stock, reserved_stock)
VALUES ('VLR-SKT-BLK-M', 'PRD-005', 'Black', 'M', 219000.00, 9, 0)
ON DUPLICATE KEY UPDATE product_id=VALUES(product_id), color_name=VALUES(color_name), size_name=VALUES(size_name), price=VALUES(price), available_stock=VALUES(available_stock), reserved_stock=VALUES(reserved_stock);

INSERT INTO product_variants (sku, product_id, color_name, size_name, price, available_stock, reserved_stock)
VALUES ('VLR-BMB-OLV-M', 'PRD-006', 'Olive', 'M', 329000.00, 6, 0)
ON DUPLICATE KEY UPDATE product_id=VALUES(product_id), color_name=VALUES(color_name), size_name=VALUES(size_name), price=VALUES(price), available_stock=VALUES(available_stock), reserved_stock=VALUES(reserved_stock);

INSERT INTO product_variants (sku, product_id, color_name, size_name, price, available_stock, reserved_stock)
VALUES ('VLR-BMB-BLK-L', 'PRD-006', 'Black', 'L', 339000.00, 5, 0)
ON DUPLICATE KEY UPDATE product_id=VALUES(product_id), color_name=VALUES(color_name), size_name=VALUES(size_name), price=VALUES(price), available_stock=VALUES(available_stock), reserved_stock=VALUES(reserved_stock);