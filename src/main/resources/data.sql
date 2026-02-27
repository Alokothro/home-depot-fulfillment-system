-- Sample data initialization script for Home Depot Order Fulfillment System

-- Insert Warehouses
INSERT INTO warehouses (name, address_line1, city, state, zip_code, capacity, current_utilization, created_at, updated_at)
VALUES
    ('Atlanta Distribution Center', '1000 Warehouse Blvd', 'Atlanta', 'GA', '30301', 50000, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Chicago Fulfillment Center', '2500 Industrial Dr', 'Chicago', 'IL', '60601', 45000, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Dallas Regional Hub', '3200 Commerce St', 'Dallas', 'TX', '75201', 60000, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Los Angeles Distribution', '4700 Port Ave', 'Los Angeles', 'CA', '90001', 55000, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('New York Metro Center', '5100 Logistics Pkwy', 'Newark', 'NJ', '07101', 40000, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert Products
INSERT INTO products (sku, name, description, category, price, stock_quantity, warehouse_location, weight, dimensions, created_at, updated_at)
VALUES
    ('HD-PWR-001', 'DeWalt 20V Cordless Drill', 'Professional-grade cordless drill with 2 batteries', 'Power Tools', 149.99, 0, 'A-12-5', 4.5, '12x10x4 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('HD-PWR-002', 'Milwaukee Circular Saw', '7-1/4 inch circular saw with laser guide', 'Power Tools', 199.99, 0, 'A-12-6', 8.2, '15x12x8 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('HD-LBR-001', '2x4x8 Pressure Treated Lumber', 'Pressure treated pine lumber for outdoor use', 'Lumber', 8.97, 0, 'B-5-1', 18.0, '96x3.5x1.5 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('HD-LBR-002', 'Plywood 4x8 Sheet', '3/4 inch birch plywood sheet', 'Lumber', 54.99, 0, 'B-5-2', 45.0, '96x48x0.75 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('HD-PNT-001', 'Behr Premium Paint Gallon', 'Interior/Exterior premium paint, white', 'Paint', 39.99, 0, 'C-8-3', 11.5, '8x8x8 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('HD-PNT-002', 'Paint Roller Set', 'Professional 9-inch roller with frame and tray', 'Paint', 24.99, 0, 'C-8-4', 2.0, '14x10x3 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('HD-HRD-001', 'Door Lock Set', 'Schlage deadbolt and handle set, brushed nickel', 'Hardware', 79.99, 0, 'D-3-2', 3.5, '10x8x3 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('HD-HRD-002', 'Cabinet Hinges 50-Pack', 'Self-closing overlay cabinet hinges', 'Hardware', 34.99, 0, 'D-3-3', 5.0, '8x6x4 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('HD-GRD-001', 'Scotts Lawn Fertilizer', '5000 sq ft coverage lawn fertilizer', 'Garden', 29.99, 0, 'E-10-1', 15.0, '18x12x6 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('HD-GRD-002', 'Garden Hose 50ft', 'Heavy-duty 5/8 inch garden hose', 'Garden', 44.99, 0, 'E-10-2', 8.0, '12x12x4 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('HD-ELE-001', 'LED Light Bulbs 8-Pack', 'A19 60W equivalent LED bulbs, daylight', 'Electrical', 19.99, 0, 'F-7-1', 1.2, '8x6x4 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('HD-ELE-002', 'Extension Cord 25ft', 'Heavy-duty 14-gauge outdoor extension cord', 'Electrical', 24.99, 0, 'F-7-2', 3.5, '10x8x3 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('HD-PLM-001', 'Kitchen Faucet', 'Single-handle pull-down kitchen faucet, chrome', 'Plumbing', 129.99, 0, 'G-4-1', 6.0, '18x12x8 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('HD-PLM-002', 'PVC Pipe 10ft', '1-inch diameter PVC pipe', 'Plumbing', 7.99, 0, 'G-4-2', 2.5, '120x1x1 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('HD-FLR-001', 'Vinyl Plank Flooring', 'Waterproof luxury vinyl plank, 20 sq ft per box', 'Flooring', 44.99, 0, 'H-6-1', 32.0, '48x8x4 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('HD-FLR-002', 'Carpet Tiles 12-Pack', 'Commercial-grade carpet tiles, gray', 'Flooring', 89.99, 0, 'H-6-2', 25.0, '24x24x12 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('HD-TLS-001', 'Hammer 16oz', 'Fiberglass handle claw hammer', 'Hand Tools', 19.99, 0, 'I-9-1', 1.5, '13x5x2 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('HD-TLS-002', 'Screwdriver Set', '10-piece precision screwdriver set', 'Hand Tools', 29.99, 0, 'I-9-2', 2.0, '12x8x2 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('HD-SAF-001', 'Safety Glasses', 'ANSI-rated clear safety glasses', 'Safety', 9.99, 0, 'J-11-1', 0.3, '7x3x2 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('HD-SAF-002', 'Work Gloves Large', 'Leather palm work gloves, 3-pair pack', 'Safety', 14.99, 0, 'J-11-2', 0.8, '10x6x2 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert Inventory (distribute products across warehouses)
INSERT INTO inventory (product_id, warehouse_id, quantity, minimum_stock_level, last_restocked, created_at, updated_at)
VALUES
    -- Atlanta Distribution Center (ID: 1)
    (1, 1, 150, 20, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 1, 120, 15, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (5, 1, 300, 50, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (9, 1, 200, 30, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (13, 1, 80, 10, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (17, 1, 250, 40, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    -- Chicago Fulfillment Center (ID: 2)
    (1, 2, 180, 20, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3, 2, 500, 100, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (6, 2, 220, 35, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (10, 2, 150, 25, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (14, 2, 400, 60, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (18, 2, 180, 30, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    -- Dallas Regional Hub (ID: 3)
    (2, 3, 100, 15, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (4, 3, 350, 80, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (7, 3, 140, 20, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (11, 3, 500, 80, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (15, 3, 280, 50, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (19, 3, 600, 100, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    -- Los Angeles Distribution (ID: 4)
    (1, 4, 200, 20, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3, 4, 450, 100, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (8, 4, 190, 30, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (12, 4, 280, 45, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (16, 4, 220, 40, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (20, 4, 350, 60, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    -- New York Metro Center (ID: 5)
    (5, 5, 280, 50, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (6, 5, 190, 35, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (9, 5, 240, 30, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (10, 5, 160, 25, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (13, 5, 95, 10, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (17, 5, 220, 40, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Update warehouse utilization based on inventory
UPDATE warehouses SET current_utilization = (
    SELECT COALESCE(SUM(quantity), 0) FROM inventory WHERE warehouse_id = warehouses.warehouse_id
);

-- Insert Sample Customers
INSERT INTO customers (first_name, last_name, email, phone, address_line1, address_line2, city, state, zip_code, account_created_date, updated_at)
VALUES
    ('John', 'Smith', 'john.smith@email.com', '4045551234', '123 Main St', 'Apt 4B', 'Atlanta', 'GA', '30301', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Sarah', 'Johnson', 'sarah.johnson@email.com', '3125552345', '456 Oak Ave', NULL, 'Chicago', 'IL', '60601', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Michael', 'Williams', 'michael.w@email.com', '2145553456', '789 Elm Street', 'Suite 100', 'Dallas', 'TX', '75201', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
