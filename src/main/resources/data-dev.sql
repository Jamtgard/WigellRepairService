insert into repair_technicians (repair_technicians_name, repair_technicians_speciality, active)
values
    ('Lina Karlsson', 'ELECTRONICS', true),
    ('Kurt Kobein',   'CAR',         true),
    ('Eva Sundberg',  'APPLIANCE',   true),
    ('Gösta Sundbom', 'ELECTRONICS', true),
    ('Maja Hermansson','CAR',        true),
    ('Gunnar Kilhelm', 'APPLIANCE', true);

insert into repair_service (name, repair_service_type, price_sek, technician_id, is_active)
values
    ('Skärmbyte mobil', 'ELECTRONICS', 1290.00, 1, true),
    ('Laptop felsök',   'ELECTRONICS',  890.00,  1, true),
    ('Bromsbyte',       'CAR',          4590.00, 2, true),
    ('Däckbyte',        'CAR',          790.00,  2, true),
    ('Kylskåpsservice', 'APPLIANCE',    1490.00, 3, true),
    ('Diskmaskin rep',  'APPLIANCE',    1890.00, 3, true);

insert into repair_customer (name)
values
    ('alex'),
    ('sara'),
    ('amanda');

-- Alex - Bookings:
insert into repair_booking (customer_id, service_id, date, status, total_price_sek)
values
    (1, 3, '2025-09-01', 'COMPLETED', 4590.00),
    (1, 1, '2025-09-10', 'CANCELED', 1290.00),
    (1, 5, '2025-09-25', 'ON_GOING', 1490.00),
    (1, 4, '2025-09-30', 'BOOKED', 790.00),
    (1, 6, '2025-10-05', 'BOOKED', 1890.00);

-- Sara - Bookings:
insert into repair_booking (customer_id, service_id, date, status, total_price_sek)
values
    (2, 2, '2025-08-20', 'COMPLETED', 890.00),
    (2, 4, '2025-09-05', 'CANCELED', 790.00),
    (2, 3, '2025-09-25', 'ON_GOING', 4590.00),
    (2, 1, '2025-10-01', 'BOOKED', 1290.00),
    (2, 6, '2025-10-08', 'BOOKED', 1890.00);

-- Amanda -- Bookings:
insert into repair_booking (customer_id, service_id, date, status, total_price_sek)
values
    (3, 6, '2025-07-30', 'COMPLETED', 1890.00),
    (3, 1, '2025-09-02', 'COMPLETED', 1290.00),
    (3, 2, '2025-09-25', 'ON_GOING', 890.00),
    (3, 3, '2025-10-01', 'BOOKED', 4590.00),
    (3, 5, '2025-10-10', 'BOOKED', 1490.00);






