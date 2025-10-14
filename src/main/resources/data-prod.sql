INSERT INTO repair_customer (name)
SELECT 'alex' WHERE NOT EXISTS (SELECT 1 FROM repair_customer WHERE name='alex');

INSERT INTO repair_customer (name)
SELECT 'sara' WHERE NOT EXISTS (SELECT 1 FROM repair_customer WHERE name='sara');

INSERT INTO repair_customer (name)
SELECT 'amanda' WHERE NOT EXISTS (SELECT 1 FROM repair_customer WHERE name='amanda');