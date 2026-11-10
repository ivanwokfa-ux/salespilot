-- Une ligne rejetee lors d'un import, avec la raison du rejet
CREATE TABLE import_error (
                              id              BIGSERIAL    PRIMARY KEY,
                              import_batch_id BIGINT       NOT NULL REFERENCES import_batch (id) ON DELETE CASCADE,
                              line_number     INTEGER      NOT NULL,
                              raw_line        TEXT,
                              error_message   VARCHAR(500) NOT NULL
);

CREATE INDEX idx_import_error_batch ON import_error (import_batch_id);