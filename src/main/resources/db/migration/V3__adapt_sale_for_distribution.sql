-- 1. Anciennes regles a remplacer
ALTER TABLE sale DROP CONSTRAINT uk_sale_order_product;
ALTER TABLE sale DROP CONSTRAINT sale_quantity_check;

-- 2. Nouvelles colonnes
ALTER TABLE sale
    ADD COLUMN currency      VARCHAR(3)     NOT NULL,
    ADD COLUMN customer_code VARCHAR(64),
    ADD COLUMN customer_name VARCHAR(255),
    ADD COLUMN agency        VARCHAR(64),
    ADD COLUMN cost_amount   NUMERIC(14, 2);

-- 3. Nouvelles regles
ALTER TABLE sale ADD CONSTRAINT ck_sale_quantity_not_zero CHECK (quantity <> 0);
ALTER TABLE sale ADD CONSTRAINT ck_sale_currency_iso CHECK (currency ~ '^[A-Z]{3}$');
ALTER TABLE sale ADD CONSTRAINT uk_sale_agency_order_product
    UNIQUE NULLS NOT DISTINCT (agency, order_ref, product_sku);

CREATE INDEX idx_sale_customer_code ON sale (customer_code);