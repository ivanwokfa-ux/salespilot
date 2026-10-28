-- Historique des fichiers importes
CREATE TABLE import_batch (
                              id              BIGSERIAL    PRIMARY KEY,
                              file_name       VARCHAR(255) NOT NULL,
                              file_hash       CHAR(64)     NOT NULL UNIQUE,
                              status          VARCHAR(20)  NOT NULL,
                              total_rows      INTEGER      NOT NULL DEFAULT 0,
                              imported_rows   INTEGER      NOT NULL DEFAULT 0,
                              rejected_rows   INTEGER      NOT NULL DEFAULT 0,
                              duplicate_rows  INTEGER      NOT NULL DEFAULT 0,
                              created_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- Une ligne = un produit vendu dans une commande
CREATE TABLE sale (
                      id              BIGSERIAL      PRIMARY KEY,
                      import_batch_id BIGINT         NOT NULL REFERENCES import_batch (id),
                      order_ref       VARCHAR(64)    NOT NULL,
                      order_date      DATE           NOT NULL,
                      product_sku     VARCHAR(64)    NOT NULL,
                      product_name    VARCHAR(255)   NOT NULL,
                      quantity        INTEGER        NOT NULL CHECK (quantity > 0),
                      unit_price      NUMERIC(12, 2) NOT NULL CHECK (unit_price >= 0),
                      line_total      NUMERIC(14, 2) NOT NULL,
                      created_at      TIMESTAMPTZ    NOT NULL DEFAULT now(),
                      CONSTRAINT uk_sale_order_product UNIQUE (order_ref, product_sku)
);

CREATE INDEX idx_sale_order_date  ON sale (order_date);
CREATE INDEX idx_sale_product_sku ON sale (product_sku);