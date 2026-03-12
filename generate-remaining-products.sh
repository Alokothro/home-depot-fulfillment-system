#!/bin/bash

# Add remaining departments to products-data.sql

# ELECTRICAL (20 items)
cat >> /Users/alokpatel/Desktop/order-fulfillment-system/src/main/resources/products-data.sql << 'EOF'

-- ELECTRICAL (20 items)
INSERT INTO products (sku, name, description, category, price, stock_quantity, warehouse_location, department, weight, dimensions, created_at, updated_at)
VALUES
('HD-ELE-001', 'LED Light Bulbs 8-Pack 60W', 'A19 60W equivalent LED bulbs, daylight', 'Electrical', 19.99, 0, 'F-7-1', 'Electrical', 1.2, '8x6x4 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-ELE-002', 'Extension Cord 25ft Heavy Duty', '14-gauge outdoor extension cord', 'Electrical', 24.99, 0, 'F-7-2', 'Electrical', 3.5, '10x8x3 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-ELE-003', 'Ceiling Fan with Light 52 inch', '52-inch indoor ceiling fan with light kit', 'Electrical', 149.99, 0, 'F-7-3', 'Electrical', 18.0, '24x24x16 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-ELE-004', 'Wall Outlet 10-Pack', 'Duplex receptacle outlets white', 'Electrical', 12.99, 0, 'F-7-4', 'Electrical', 1.5, '6x6x3 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-ELE-005', 'Light Switch 10-Pack', 'Toggle light switches white', 'Electrical', 9.99, 0, 'F-7-5', 'Electrical', 1.2, '6x6x3 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-ELE-006', 'Wire Nuts Assortment 150-Piece', 'Electrical wire connectors assorted sizes', 'Electrical', 14.99, 0, 'F-7-6', 'Electrical', 1.0, '8x6x3 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-ELE-007', 'Electrical Tape Black 10-Pack', 'Vinyl electrical tape rolls', 'Electrical', 11.99, 0, 'F-7-7', 'Electrical', 1.5, '10x8x2 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-ELE-008', 'Motion Sensor Light Switch', 'Occupancy sensing light switch', 'Electrical', 34.99, 0, 'F-7-8', 'Electrical', 0.5, '4x3x2 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-ELE-009', 'Outdoor Flood Light LED 2-Pack', 'LED security flood lights', 'Electrical', 59.99, 0, 'F-7-9', 'Electrical', 4.0, '12x8x6 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-ELE-010', 'Circuit Breaker 20 Amp', 'Single pole circuit breaker', 'Electrical', 16.99, 0, 'F-7-10', 'Electrical', 0.8, '4x2x3 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-ELE-011', 'GFCI Outlet 2-Pack', 'Ground fault circuit interrupter outlets', 'Electrical', 29.99, 0, 'F-7-11', 'Electrical', 0.9, '6x4x3 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-ELE-012', 'Track Lighting Kit 4-Light', 'Adjustable track lighting system', 'Electrical', 89.99, 0, 'F-7-12', 'Electrical', 6.0, '48x8x6 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-ELE-013', 'Dimmer Switch', 'LED compatible dimmer switch', 'Electrical', 24.99, 0, 'F-7-13', 'Electrical', 0.4, '4x3x2 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-ELE-014', 'Smoke Detector 3-Pack', 'Battery operated smoke alarms', 'Electrical', 39.99, 0, 'F-7-14', 'Electrical', 2.0, '10x8x4 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-ELE-015', 'Doorbell Kit Wireless', 'Wireless doorbell with chime', 'Electrical', 29.99, 0, 'F-7-15', 'Electrical', 1.2, '8x6x3 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-ELE-016', 'Electrical Wire 100ft 12/2', 'Romex electrical wire', 'Electrical', 44.99, 0, 'F-7-16', 'Electrical', 8.0, '12x12x4 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-ELE-017', 'Pendant Light Fixture', 'Modern pendant light with glass shade', 'Electrical', 79.99, 0, 'F-7-17', 'Electrical', 5.0, '12x12x18 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-ELE-018', 'Surge Protector Power Strip', '12-outlet surge protector with USB', 'Electrical', 34.99, 0, 'F-7-18', 'Electrical', 2.0, '14x4x2 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-ELE-019', 'Recessed Light 6-Pack', 'LED recessed lighting retrofit kit', 'Electrical', 99.99, 0, 'F-7-19', 'Electrical', 6.0, '12x10x6 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-ELE-020', 'Outdoor String Lights 48ft', 'LED commercial grade string lights', 'Electrical', 49.99, 0, 'F-7-20', 'Electrical', 4.5, '14x10x6 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
EOF

