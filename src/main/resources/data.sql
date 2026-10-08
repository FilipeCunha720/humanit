INSERT INTO client (first_name, last_name, tax_identifier, email, phone_number)
VALUES ('John', 'Doe', 'TAX-1001', 'john.doe@example.com', '+1-555-0101');

INSERT INTO client (first_name, last_name, tax_identifier, email, phone_number)
VALUES ('Jane', 'Smith', 'TAX-1002', 'jane.smith@example.com', '+1-555-0102');

INSERT INTO document (number, description, expiration_date, client_id)
VALUES ('DOC-5501', 'Passport', '2030-05-15', 1);
INSERT INTO document (number, description, expiration_date, client_id)
VALUES ('DOC-5502', 'Driving License', '2028-11-30', 1);
INSERT INTO document (number, description, expiration_date, client_id)
VALUES ('DOC-7701', 'ID Card', '2029-01-20', 2);
