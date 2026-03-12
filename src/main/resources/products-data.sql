-- Comprehensive Home Depot Product Database
-- 20 items per department x 8 departments = 160 products

INSERT INTO products (sku, name, description, category, price, stock_quantity, warehouse_location, department, weight, dimensions, created_at, updated_at)
VALUES
-- GARDEN CENTER (20 items)
('HD-GRD-001', 'Scotts Lawn Fertilizer 15M', '5000 sq ft coverage lawn fertilizer', 'Garden', 29.99, 0, 'E-10-1', 'Garden Center', 15.0, '18x12x6 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-GRD-002', 'Garden Hose 50ft', 'Heavy-duty 5/8 inch garden hose', 'Garden', 44.99, 0, 'E-10-2', 'Garden Center', 8.0, '12x12x4 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-GRD-003', 'Miracle-Gro Potting Mix 2 cu ft', 'All purpose potting soil', 'Garden', 12.99, 0, 'E-10-3', 'Garden Center', 25.0, '24x16x6 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-GRD-004', 'Round Point Shovel', 'Fiberglass handle garden shovel', 'Garden', 34.99, 0, 'E-10-4', 'Garden Center', 5.5, '48x10x4 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-GRD-005', 'Garden Rake', 'Steel bow rake with wood handle', 'Garden', 24.99, 0, 'E-10-5', 'Garden Center', 3.2, '60x12x3 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-GRD-006', 'Pruning Shears', 'Bypass pruner with comfort grip', 'Garden', 19.99, 0, 'E-10-6', 'Garden Center', 0.8, '9x3x1 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-GRD-007', 'Wheelbarrow 6 cu ft', 'Heavy-duty poly wheelbarrow', 'Garden', 89.99, 0, 'E-10-7', 'Garden Center', 28.0, '58x26x27 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-GRD-008', 'Weed Killer 1 Gallon', 'Roundup weed and grass killer', 'Garden', 39.99, 0, 'E-10-8', 'Garden Center', 9.5, '10x6x12 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-GRD-009', 'Garden Gloves 3-Pack', 'Heavy-duty gardening gloves', 'Garden', 14.99, 0, 'E-10-9', 'Garden Center', 0.6, '10x8x2 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-GRD-010', 'Sprinkler System Kit', 'Automatic lawn sprinkler with timer', 'Garden', 79.99, 0, 'E-10-10', 'Garden Center', 12.0, '16x12x8 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-GRD-011', 'Plant Stakes 6ft 25-Pack', 'Bamboo garden stakes', 'Garden', 16.99, 0, 'E-10-11', 'Garden Center', 8.0, '72x4x4 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-GRD-012', 'Garden Hoe', 'Field hoe with fiberglass handle', 'Garden', 29.99, 0, 'E-10-12', 'Garden Center', 4.0, '54x7x3 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-GRD-013', 'Grass Seed 7 lbs', 'Sun and shade grass seed mix', 'Garden', 24.99, 0, 'E-10-13', 'Garden Center', 7.0, '15x10x4 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-GRD-014', 'Garden Kneeler Pad', 'Foam kneeling pad for gardening', 'Garden', 12.99, 0, 'E-10-14', 'Garden Center', 1.2, '18x11x2 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-GRD-015', 'Watering Can 2 Gallon', 'Plastic watering can with rose', 'Garden', 18.99, 0, 'E-10-15', 'Garden Center', 2.5, '16x9x14 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-GRD-016', 'Landscape Fabric 3x50ft', 'Weed barrier landscape fabric', 'Garden', 34.99, 0, 'E-10-16', 'Garden Center', 5.0, '50x6x6 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-GRD-017', 'Garden Hose Nozzle', '8-pattern spray nozzle', 'Garden', 14.99, 0, 'E-10-17', 'Garden Center', 0.7, '8x4x2 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-GRD-018', 'Compost Bin 80 Gallon', 'Outdoor composting bin', 'Garden', 99.99, 0, 'E-10-18', 'Garden Center', 15.0, '32x32x40 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-GRD-019', 'Garden Tool Set 3-Piece', 'Trowel, transplanter, cultivator set', 'Garden', 22.99, 0, 'E-10-19', 'Garden Center', 2.0, '14x6x2 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-GRD-020', 'Peat Moss 3 cu ft', 'Compressed peat moss bale', 'Garden', 19.99, 0, 'E-10-20', 'Garden Center', 20.0, '24x14x12 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- TOOLS & HARDWARE (20 items)
('HD-TLS-001', 'DeWalt 20V Cordless Drill', 'Professional-grade cordless drill with 2 batteries', 'Power Tools', 149.99, 0, 'A-12-1', 'Tools & Hardware', 4.5, '12x10x4 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-TLS-002', 'Milwaukee Circular Saw', '7-1/4 inch circular saw with laser guide', 'Power Tools', 199.99, 0, 'A-12-2', 'Tools & Hardware', 8.2, '15x12x8 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-TLS-003', 'Hammer 16oz', 'Fiberglass handle claw hammer', 'Hand Tools', 19.99, 0, 'A-12-3', 'Tools & Hardware', 1.5, '13x5x2 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-TLS-004', 'Screwdriver Set 10-Piece', 'Precision screwdriver set', 'Hand Tools', 29.99, 0, 'A-12-4', 'Tools & Hardware', 2.0, '12x8x2 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-TLS-005', 'Adjustable Wrench 12 inch', 'Chrome-plated adjustable wrench', 'Hand Tools', 24.99, 0, 'A-12-5', 'Tools & Hardware', 1.8, '12x3x1 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-TLS-006', 'Socket Set 230-Piece', 'Mechanics tool set with case', 'Hand Tools', 189.99, 0, 'A-12-6', 'Tools & Hardware', 18.0, '20x14x6 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-TLS-007', 'Tape Measure 25ft', 'Self-locking tape measure', 'Hand Tools', 12.99, 0, 'A-12-7', 'Tools & Hardware', 0.9, '5x5x2 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-TLS-008', 'Level 24 inch', 'Aluminum box beam level', 'Hand Tools', 34.99, 0, 'A-12-8', 'Tools & Hardware', 2.2, '24x3x2 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-TLS-009', 'Utility Knife', 'Retractable utility knife with blade storage', 'Hand Tools', 9.99, 0, 'A-12-9', 'Tools & Hardware', 0.4, '7x2x1 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-TLS-010', 'Pliers Set 3-Piece', 'Needle nose, slip joint, groove joint pliers', 'Hand Tools', 39.99, 0, 'A-12-10', 'Tools & Hardware', 2.5, '12x8x2 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-TLS-011', 'Power Drill Bit Set 100-Piece', 'Titanium drill bit set', 'Power Tool Accessories', 49.99, 0, 'A-12-11', 'Tools & Hardware', 5.0, '12x10x3 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-TLS-012', 'Stud Finder', 'Electronic wall scanner', 'Hand Tools', 29.99, 0, 'A-12-12', 'Tools & Hardware', 0.6, '6x3x2 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-TLS-013', 'Safety Glasses', 'ANSI-rated clear safety glasses', 'Safety', 9.99, 0, 'A-12-13', 'Tools & Hardware', 0.3, '7x3x2 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-TLS-014', 'Work Gloves Large 3-Pack', 'Leather palm work gloves', 'Safety', 14.99, 0, 'A-12-14', 'Tools & Hardware', 0.8, '10x6x2 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-TLS-015', 'Tool Box 22 inch', 'Heavy-duty plastic tool box', 'Storage', 44.99, 0, 'A-12-15', 'Tools & Hardware', 7.0, '22x11x10 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-TLS-016', 'Extension Cord 50ft', '12-gauge outdoor extension cord', 'Electrical', 39.99, 0, 'A-12-16', 'Tools & Hardware', 6.5, '12x10x3 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-TLS-017', 'Cabinet Hinges 50-Pack', 'Self-closing overlay cabinet hinges', 'Hardware', 34.99, 0, 'A-12-17', 'Tools & Hardware', 5.0, '8x6x4 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-TLS-018', 'Brad Nailer Pneumatic', 'Air-powered brad nailer', 'Power Tools', 129.99, 0, 'A-12-18', 'Tools & Hardware', 3.5, '12x10x4 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-TLS-019', 'Hacksaw with Blades', 'Adjustable hacksaw frame with 10 blades', 'Hand Tools', 16.99, 0, 'A-12-19', 'Tools & Hardware', 1.5, '14x4x2 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-TLS-020', 'Multi-Tool Oscillating', 'Corded oscillating multi-tool', 'Power Tools', 79.99, 0, 'A-12-20', 'Tools & Hardware', 3.2, '12x8x5 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- PAINT (20 items)
INSERT INTO products (sku, name, description, category, price, stock_quantity, warehouse_location, department, weight, dimensions, created_at, updated_at)
VALUES
('HD-PNT-001', 'Behr Premium Plus Paint Gallon White', 'Interior/Exterior premium paint, white', 'Paint', 39.99, 0, 'C-8-1', 'Paint', 11.5, '8x8x8 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-PNT-002', 'Paint Roller Set', 'Professional 9-inch roller with frame and tray', 'Paint', 24.99, 0, 'C-8-2', 'Paint', 2.0, '14x10x3 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-PNT-003', 'Paintbrush Set 5-Piece', 'Angled brush set for trim and walls', 'Paint', 19.99, 0, 'C-8-3', 'Paint', 1.0, '12x6x2 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-PNT-004', 'Paint Sprayer Airless', 'Electric paint sprayer for indoor/outdoor', 'Paint', 249.99, 0, 'C-8-4', 'Paint', 14.0, '18x12x12 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-PNT-005', 'Drop Cloth 9x12ft', 'Canvas drop cloth', 'Paint', 29.99, 0, 'C-8-5', 'Paint', 4.0, '12x12x3 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-PNT-006', 'Painters Tape 1.5 inch', 'Blue painters tape 60 yard roll', 'Paint', 8.99, 0, 'C-8-6', 'Paint', 0.8, '6x6x2 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-PNT-007', 'Paint Tray Liners 10-Pack', 'Disposable paint tray liners', 'Paint', 6.99, 0, 'C-8-7', 'Paint', 0.5, '10x8x2 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-PNT-008', 'Primer Gallon', 'Interior drywall primer sealer', 'Paint', 34.99, 0, 'C-8-8', 'Paint', 11.0, '8x8x8 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-PNT-009', 'Paint Edger Tool', 'Paint edger with guide wheels', 'Paint', 14.99, 0, 'C-8-9', 'Paint', 0.6, '10x4x3 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-PNT-010', 'Wood Stain Quart Dark Walnut', 'Oil-based wood stain', 'Paint', 16.99, 0, 'C-8-10', 'Paint', 2.5, '6x6x6 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-PNT-011', 'Spray Paint 6-Pack', 'Assorted colors spray paint', 'Paint', 24.99, 0, 'C-8-11', 'Paint', 4.0, '12x8x8 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-PNT-012', 'Paint Mixing Sticks 50-Pack', 'Wooden paint stirrers', 'Paint', 4.99, 0, 'C-8-12', 'Paint', 2.0, '14x4x2 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-PNT-013', 'Caulk Gun', 'Smooth rod caulking gun', 'Paint', 9.99, 0, 'C-8-13', 'Paint', 1.2, '12x4x2 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-PNT-014', 'Sandpaper Assortment 25-Sheet', 'Mixed grit sandpaper pack', 'Paint', 12.99, 0, 'C-8-14', 'Paint', 1.5, '11x9x1 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-PNT-015', 'Paint Stripper Quart', 'Gel paint and varnish remover', 'Paint', 18.99, 0, 'C-8-15', 'Paint', 2.8, '6x4x8 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-PNT-016', 'Roller Covers 9 inch 6-Pack', 'High density roller covers', 'Paint', 16.99, 0, 'C-8-16', 'Paint', 1.0, '10x6x4 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-PNT-017', 'Paint Bucket 5 Gallon', 'Plastic paint bucket with lid', 'Paint', 7.99, 0, 'C-8-17', 'Paint', 2.0, '12x12x14 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-PNT-018', 'Putty Knife Set 4-Piece', 'Flexible putty knife set', 'Paint', 14.99, 0, 'C-8-18', 'Paint', 0.8, '10x6x1 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-PNT-019', 'Paint Brush Cleaner', 'Brush and roller cleaner solution', 'Paint', 11.99, 0, 'C-8-19', 'Paint', 2.5, '8x4x10 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-PNT-020', 'Texture Paint Additive', 'Sand texture paint additive', 'Paint', 9.99, 0, 'C-8-20', 'Paint', 3.0, '8x6x4 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

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

-- PLUMBING, KITCHEN & BATH (20 items)
INSERT INTO products (sku, name, description, category, price, stock_quantity, warehouse_location, department, weight, dimensions, created_at, updated_at)
VALUES
('HD-PLM-001', 'Kitchen Faucet Single Handle', 'Chrome pull-down kitchen faucet', 'Plumbing', 129.99, 0, 'D-9-1', 'Plumbing, Kitchen & Bath', 5.0, '16x10x6 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-PLM-002', 'Bathroom Vanity 24 inch', 'White bathroom vanity with marble top', 'Bathroom', 399.99, 0, 'D-9-2', 'Plumbing, Kitchen & Bath', 85.0, '24x22x34 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-PLM-003', 'Toilet Dual Flush', 'Water-efficient dual flush toilet', 'Plumbing', 249.99, 0, 'D-9-3', 'Plumbing, Kitchen & Bath', 95.0, '28x18x30 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-PLM-004', 'Shower Head Rainfall', '12-inch rainfall shower head with arm', 'Plumbing', 89.99, 0, 'D-9-4', 'Plumbing, Kitchen & Bath', 3.5, '14x14x4 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-PLM-005', 'PEX Tubing 100ft 1/2 inch', 'Red PEX water pipe', 'Plumbing', 44.99, 0, 'D-9-5', 'Plumbing, Kitchen & Bath', 12.0, '18x18x4 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-PLM-006', 'Sink Drain Kit', 'Pop-up sink drain assembly chrome', 'Plumbing', 24.99, 0, 'D-9-6', 'Plumbing, Kitchen & Bath', 1.5, '10x6x4 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-PLM-007', 'Water Heater 50 Gallon Electric', 'Residential electric water heater', 'Plumbing', 599.99, 0, 'D-9-7', 'Plumbing, Kitchen & Bath', 120.0, '22x22x60 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-PLM-008', 'Plunger and Auger Set', 'Toilet plunger with drain auger', 'Plumbing', 19.99, 0, 'D-9-8', 'Plumbing, Kitchen & Bath', 3.0, '24x8x8 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-PLM-009', 'Faucet Repair Kit Universal', 'Universal faucet repair kit', 'Plumbing', 16.99, 0, 'D-9-9', 'Plumbing, Kitchen & Bath', 0.8, '8x6x2 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-PLM-010', 'Garbage Disposal 3/4 HP', 'Continuous feed garbage disposal', 'Plumbing', 149.99, 0, 'D-9-10', 'Plumbing, Kitchen & Bath', 18.0, '10x8x14 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-PLM-011', 'Bathtub Faucet Set', '3-handle tub and shower faucet chrome', 'Plumbing', 179.99, 0, 'D-9-11', 'Plumbing, Kitchen & Bath', 8.0, '18x12x6 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-PLM-012', 'PVC Pipe Fittings 50-Pack', 'Assorted PVC pipe connectors', 'Plumbing', 34.99, 0, 'D-9-12', 'Plumbing, Kitchen & Bath', 6.0, '14x10x8 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-PLM-013', 'Shut-Off Valve 5-Pack', 'Quarter turn ball valves 1/2 inch', 'Plumbing', 29.99, 0, 'D-9-13', 'Plumbing, Kitchen & Bath', 2.5, '8x6x4 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-PLM-014', 'Toilet Seat Elongated White', 'Slow-close toilet seat', 'Bathroom', 39.99, 0, 'D-9-14', 'Plumbing, Kitchen & Bath', 4.5, '19x14x3 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-PLM-015', 'Kitchen Sink Stainless Steel', 'Double bowl undermount sink', 'Plumbing', 199.99, 0, 'D-9-15', 'Plumbing, Kitchen & Bath', 25.0, '33x22x9 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-PLM-016', 'Shower Valve Trim Kit', 'Pressure balance shower valve trim', 'Plumbing', 79.99, 0, 'D-9-16', 'Plumbing, Kitchen & Bath', 3.0, '10x8x6 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-PLM-017', 'Pipe Wrench 18 inch', 'Heavy-duty pipe wrench', 'Plumbing', 44.99, 0, 'D-9-17', 'Plumbing, Kitchen & Bath', 4.5, '18x4x2 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-PLM-018', 'Water Filter Whole House', 'Whole house water filtration system', 'Plumbing', 199.99, 0, 'D-9-18', 'Plumbing, Kitchen & Bath', 15.0, '20x8x8 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-PLM-019', 'Bathroom Mirror 24x30', 'Frameless rectangular bathroom mirror', 'Bathroom', 79.99, 0, 'D-9-19', 'Plumbing, Kitchen & Bath', 12.0, '30x24x2 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-PLM-020', 'Sump Pump 1/3 HP', 'Submersible sump pump', 'Plumbing', 129.99, 0, 'D-9-20', 'Plumbing, Kitchen & Bath', 14.0, '12x10x14 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- FLOORING (20 items)
INSERT INTO products (sku, name, description, category, price, stock_quantity, warehouse_location, department, weight, dimensions, created_at, updated_at)
VALUES
('HD-FLR-001', 'Laminate Flooring Oak 20 sq ft', 'AC4 rated oak laminate flooring box', 'Flooring', 39.99, 0, 'B-14-1', 'Flooring', 32.0, '50x12x6 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-FLR-002', 'Vinyl Plank Flooring 24 sq ft', 'Waterproof luxury vinyl plank', 'Flooring', 54.99, 0, 'B-14-2', 'Flooring', 38.0, '48x12x6 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-FLR-003', 'Hardwood Flooring Maple 20 sq ft', 'Solid maple hardwood flooring', 'Flooring', 89.99, 0, 'B-14-3', 'Flooring', 42.0, '50x10x6 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-FLR-004', 'Carpet Tile 12-Pack', 'Peel and stick carpet tiles', 'Flooring', 44.99, 0, 'B-14-4', 'Flooring', 15.0, '24x18x6 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-FLR-005', 'Floor Underlayment 100 sq ft', 'Foam underlayment roll', 'Flooring', 29.99, 0, 'B-14-5', 'Flooring', 8.0, '48x12x12 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-FLR-006', 'Tile Grout 25 lbs', 'Sanded tile grout gray', 'Flooring', 19.99, 0, 'B-14-6', 'Flooring', 25.0, '16x10x4 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-FLR-007', 'Ceramic Floor Tile 10-Pack', '12x12 inch ceramic tiles', 'Flooring', 34.99, 0, 'B-14-7', 'Flooring', 35.0, '14x14x8 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-FLR-008', 'Tile Spacers 500-Pack', 'Cross tile spacers 1/8 inch', 'Flooring', 9.99, 0, 'B-14-8', 'Flooring', 2.0, '8x6x2 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-FLR-009', 'Floor Leveler 50 lbs', 'Self-leveling floor underlayment', 'Flooring', 39.99, 0, 'B-14-9', 'Flooring', 50.0, '20x14x4 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-FLR-010', 'Baseboard Molding 8ft White', 'MDF baseboard trim', 'Flooring', 14.99, 0, 'B-14-10', 'Flooring', 6.0, '96x5x1 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-FLR-011', 'Transition Strip Multi-Purpose', 'T-molding floor transition strip', 'Flooring', 24.99, 0, 'B-14-11', 'Flooring', 2.0, '72x2x1 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-FLR-012', 'Floor Adhesive Gallon', 'Multi-purpose flooring adhesive', 'Flooring', 44.99, 0, 'B-14-12', 'Flooring', 11.0, '8x6x10 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-FLR-013', 'Trowel Notched 1/4 inch', 'Square-notch tile trowel', 'Flooring', 16.99, 0, 'B-14-13', 'Flooring', 1.5, '12x5x2 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-FLR-014', 'Rubber Stair Tread 4-Pack', 'Non-slip rubber stair treads', 'Flooring', 49.99, 0, 'B-14-14', 'Flooring', 8.0, '36x10x2 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-FLR-015', 'Floor Scraper Heavy Duty', 'Long handle floor scraper', 'Flooring', 34.99, 0, 'B-14-15', 'Flooring', 5.0, '48x6x3 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-FLR-016', 'Flooring Nailer Pneumatic', 'Air-powered flooring nailer', 'Flooring', 299.99, 0, 'B-14-16', 'Flooring', 11.0, '16x12x8 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-FLR-017', 'Floor Polish 32 oz', 'Hardwood floor polish and restorer', 'Flooring', 19.99, 0, 'B-14-17', 'Flooring', 2.5, '10x4x8 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-FLR-018', 'Knee Pads Professional', 'Heavy-duty flooring knee pads', 'Flooring', 29.99, 0, 'B-14-18', 'Flooring', 1.5, '10x8x4 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-FLR-019', 'Floor Roller 100 lbs', 'Vinyl floor roller', 'Flooring', 189.99, 0, 'B-14-19', 'Flooring', 105.0, '18x12x12 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-FLR-020', 'Area Rug 5x7 ft Gray', 'Modern geometric area rug', 'Flooring', 129.99, 0, 'B-14-20', 'Flooring', 12.0, '84x60x2 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- LUMBER & BUILDING MATERIALS (20 items)
INSERT INTO products (sku, name, description, category, price, stock_quantity, warehouse_location, department, weight, dimensions, created_at, updated_at)
VALUES
('HD-LBR-001', '2x4x8 Lumber Pressure Treated', 'Pressure treated lumber stud', 'Lumber', 8.99, 0, 'G-15-1', 'Lumber & Building Materials', 18.0, '96x3.5x1.5 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-LBR-002', 'Plywood 4x8 ft 1/2 inch', 'Birch plywood sheet', 'Lumber', 44.99, 0, 'G-15-2', 'Lumber & Building Materials', 48.0, '96x48x0.5 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-LBR-003', 'OSB Board 4x8 ft 7/16 inch', 'Oriented strand board sheathing', 'Lumber', 24.99, 0, 'G-15-3', 'Lumber & Building Materials', 50.0, '96x48x0.4 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-LBR-004', 'Concrete Mix 80 lbs', 'Fast-setting concrete mix', 'Building Materials', 6.99, 0, 'G-15-4', 'Lumber & Building Materials', 80.0, '24x16x6 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-LBR-005', 'Drywall 4x8 ft 1/2 inch', 'Gypsum drywall sheet', 'Building Materials', 12.99, 0, 'G-15-5', 'Lumber & Building Materials', 54.0, '96x48x0.5 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-LBR-006', 'Shingles 3-Tab Bundle', 'Asphalt roof shingles 33 sq ft', 'Building Materials', 34.99, 0, 'G-15-6', 'Lumber & Building Materials', 75.0, '40x16x12 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-LBR-007', 'Insulation Batt R-13', 'Fiberglass insulation roll 40 sq ft', 'Building Materials', 39.99, 0, 'G-15-7', 'Lumber & Building Materials', 15.0, '96x15x4 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-LBR-008', 'Screws 5 lbs #8x2.5 inch', 'Construction screws box', 'Hardware', 24.99, 0, 'G-15-8', 'Lumber & Building Materials', 5.0, '10x6x4 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-LBR-009', 'Framing Nails 5 lbs 16d', 'Common framing nails', 'Hardware', 19.99, 0, 'G-15-9', 'Lumber & Building Materials', 5.0, '10x6x4 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-LBR-010', 'Cedar Fence Picket 6 ft', 'Dog-ear cedar fence board', 'Lumber', 4.99, 0, 'G-15-10', 'Lumber & Building Materials', 4.0, '72x5.5x0.6 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-LBR-011', 'Landscape Timber 8 ft', 'Pressure treated landscape timber', 'Lumber', 12.99, 0, 'G-15-11', 'Lumber & Building Materials', 22.0, '96x4x4 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-LBR-012', 'Deck Board 12 ft Composite', 'Composite decking board', 'Lumber', 34.99, 0, 'G-15-12', 'Lumber & Building Materials', 28.0, '144x5.5x1 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-LBR-013', 'House Wrap 3x100 ft', 'Moisture barrier house wrap roll', 'Building Materials', 79.99, 0, 'G-15-13', 'Lumber & Building Materials', 12.0, '36x10x10 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-LBR-014', 'Joint Compound 5 Gallon', 'All-purpose drywall compound', 'Building Materials', 24.99, 0, 'G-15-14', 'Lumber & Building Materials', 55.0, '14x14x16 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-LBR-015', 'Metal Studs 10 ft 6-Pack', 'Steel framing studs 25 gauge', 'Building Materials', 44.99, 0, 'G-15-15', 'Lumber & Building Materials', 35.0, '120x4x3 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-LBR-016', 'Mortar Mix 60 lbs', 'Type S masonry mortar mix', 'Building Materials', 8.99, 0, 'G-15-16', 'Lumber & Building Materials', 60.0, '20x14x5 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-LBR-017', 'Rebar 1/2 inch x 20 ft', 'Grade 60 steel rebar', 'Building Materials', 16.99, 0, 'G-15-17', 'Lumber & Building Materials', 25.0, '240x0.5x0.5 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-LBR-018', 'Cinder Block 8x8x16', 'Standard concrete block', 'Building Materials', 2.49, 0, 'G-15-18', 'Lumber & Building Materials', 38.0, '16x8x8 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-LBR-019', 'Treated Post 4x4x8', 'Pressure treated fence post', 'Lumber', 19.99, 0, 'G-15-19', 'Lumber & Building Materials', 28.0, '96x3.5x3.5 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-LBR-020', 'Crown Molding 8 ft', 'MDF crown molding trim', 'Lumber', 18.99, 0, 'G-15-20', 'Lumber & Building Materials', 5.0, '96x4x4 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- DOORS & WINDOWS (20 items)
INSERT INTO products (sku, name, description, category, price, stock_quantity, warehouse_location, department, weight, dimensions, created_at, updated_at)
VALUES
('HD-DRW-001', 'Interior Door 30x80 6-Panel', 'Hollow core white interior door', 'Doors', 79.99, 0, 'H-11-1', 'Doors & Windows', 35.0, '80x30x1.5 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-DRW-002', 'Exterior Door Steel 36x80', 'Insulated steel entry door', 'Doors', 299.99, 0, 'H-11-2', 'Doors & Windows', 95.0, '80x36x2 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-DRW-003', 'Door Knob Set Satin Nickel', 'Privacy door knob with deadbolt', 'Hardware', 44.99, 0, 'H-11-3', 'Doors & Windows', 2.5, '10x6x4 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-DRW-004', 'Sliding Glass Door 72x80', 'Vinyl sliding patio door', 'Doors', 899.99, 0, 'H-11-4', 'Doors & Windows', 180.0, '80x72x3 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-DRW-005', 'Window Double Hung 32x48', 'Vinyl double hung window', 'Windows', 249.99, 0, 'H-11-5', 'Doors & Windows', 45.0, '48x32x4 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-DRW-006', 'Screen Door Aluminum 36x80', 'Retractable screen door', 'Doors', 149.99, 0, 'H-11-6', 'Doors & Windows', 25.0, '80x36x2 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-DRW-007', 'Window Blinds 36 inch White', 'Cordless vinyl mini blinds', 'Windows', 29.99, 0, 'H-11-7', 'Doors & Windows', 4.0, '36x64x3 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-DRW-008', 'Door Sweep 36 inch', 'Under door draft stopper', 'Hardware', 12.99, 0, 'H-11-8', 'Doors & Windows', 0.8, '36x2x2 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-DRW-009', 'Door Hinges 3.5 inch 12-Pack', 'Ball bearing door hinges satin nickel', 'Hardware', 34.99, 0, 'H-11-9', 'Doors & Windows', 4.0, '10x8x2 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-DRW-010', 'Garage Door Insulation Kit', 'Reflective foam insulation panels', 'Doors', 79.99, 0, 'H-11-10', 'Doors & Windows', 12.0, '24x18x8 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-DRW-011', 'Storm Door White 36x80', 'Full view storm door with retractable screen', 'Doors', 199.99, 0, 'H-11-11', 'Doors & Windows', 55.0, '80x36x2 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-DRW-012', 'Window Curtain Rod 48-84 inch', 'Adjustable curtain rod with finials', 'Windows', 24.99, 0, 'H-11-12', 'Doors & Windows', 3.0, '84x4x4 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-DRW-013', 'Deadbolt Smart Lock', 'Keyless entry smart deadbolt', 'Hardware', 149.99, 0, 'H-11-13', 'Doors & Windows', 2.0, '8x5x4 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-DRW-014', 'Window Screen 36x48', 'Aluminum window screen replacement', 'Windows', 19.99, 0, 'H-11-14', 'Doors & Windows', 3.0, '48x36x1 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-DRW-015', 'Door Closer Hydraulic', 'Commercial door closer', 'Hardware', 44.99, 0, 'H-11-15', 'Doors & Windows', 4.5, '12x6x4 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-DRW-016', 'Garage Door Opener 1/2 HP', 'Chain drive garage door opener with remote', 'Doors', 249.99, 0, 'H-11-16', 'Doors & Windows', 32.0, '24x18x12 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-DRW-017', 'Window Caulk Clear 6-Pack', 'Clear silicone window sealant', 'Windows', 24.99, 0, 'H-11-17', 'Doors & Windows', 4.0, '12x8x4 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-DRW-018', 'Door Frame 36 inch', 'Pre-hung door frame kit', 'Doors', 89.99, 0, 'H-11-18', 'Doors & Windows', 28.0, '82x40x6 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-DRW-019', 'Window Shades Cellular 36 inch', 'Light filtering cellular shades', 'Windows', 59.99, 0, 'H-11-19', 'Doors & Windows', 5.0, '36x64x4 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HD-DRW-020', 'Door Lockset Entry Set', 'Handleset with deadbolt oil rubbed bronze', 'Hardware', 129.99, 0, 'H-11-20', 'Doors & Windows', 6.0, '14x8x4 inches', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
