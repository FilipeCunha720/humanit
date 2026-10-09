CREATE TABLE client (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name     VARCHAR(100) NOT NULL,
    last_name      VARCHAR(100) NOT NULL,
    tax_identifier VARCHAR(50)  NOT NULL UNIQUE,
    email          VARCHAR(150) NOT NULL,
    phone_number   VARCHAR(30)
);

CREATE TABLE document (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    number          VARCHAR(100) NOT NULL,
    description     VARCHAR(255),
    expiration_date DATE,
    client_id       BIGINT NOT NULL,
    CONSTRAINT fk_document_client FOREIGN KEY (client_id) REFERENCES client (id) ON DELETE CASCADE
);

CREATE TABLE app_user (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    email         VARCHAR(254) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL
);
