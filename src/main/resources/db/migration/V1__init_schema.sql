CREATE TABLE customers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    document_cpf VARCHAR(14) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE tags (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tag_code VARCHAR(50) NOT NULL UNIQUE,
    license_plate VARCHAR(10) NOT NULL UNIQUE,
    customer_id BIGINT NOT NULL,
    balance DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tags_customer FOREIGN KEY (customer_id) REFERENCES customers (id)
);

CREATE INDEX idx_tags_code ON tags (tag_code);
CREATE INDEX idx_tags_plate ON tags (license_plate);

CREATE TABLE passage_transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tag_id BIGINT NOT NULL,
    passage_type VARCHAR(20) NOT NULL,
    location_name VARCHAR(150) NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    status VARCHAR(30) NOT NULL,
    external_conectcar_id VARCHAR(100),
    occurred_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_passages_tag FOREIGN KEY (tag_id) REFERENCES tags (id)
);

CREATE INDEX idx_passages_tag_id ON passage_transactions (tag_id);
