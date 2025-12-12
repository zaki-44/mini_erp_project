--@block
CREATE TABLE `users` (
    id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    username VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(30),
    role ENUM('ADMIN', 'CLIENT', 'DELIVERER') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE client (
    id INT PRIMARY KEY,
    address VARCHAR(255),
    FOREIGN KEY (id) REFERENCES users(id) ON DELETE CASCADE
);


CREATE TABLE deliverer (
    id INT PRIMARY KEY,
    vehicle_type ENUM('CAR', 'BIKE', 'TRUCK') NOT NULL,
    max_weight FLOAT NOT NULL,
    is_available BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE package (
    id_package INT AUTO_INCREMENT PRIMARY KEY,
    id_client_source INT NOT NULL,
    id_client_destination INT,
    vehicle_type_needed ENUM('CAR', 'BIKE', 'TRUCK'),
    address_source VARCHAR(255) NOT NULL,
    address_destination VARCHAR(255) NOT NULL,
    weight FLOAT,
    price FLOAT,
    dimensions VARCHAR(100),
    description TEXT,
    status ENUM('CREATED', 'ASSIGNED', 'PICKEDUP', 'DELIVERED', 'CANCELED') DEFAULT 'CREATED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (id_client_source) REFERENCES client(id),
    FOREIGN KEY (id_client_destination) REFERENCES client(id)
);

CREATE TABLE affectation (
    id_affectation INT AUTO_INCREMENT PRIMARY KEY,
    id_deliverer INT NOT NULL,
    id_package INT NOT NULL,
    status ENUM('PENDING', 'ACCEPTED', 'REJECTED', 'COMPLETED') NOT NULL,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (id_deliverer) REFERENCES deliverer(id),
    FOREIGN KEY (id_package) REFERENCES package(id_package)
);


CREATE TABLE notification (
    id_notification INT AUTO_INCREMENT PRIMARY KEY,
    id_package INT NOT NULL,
    id_user_target INT NOT NULL,
    message VARCHAR(255),
    type ENUM('STATUS_UPDATE', 'ASSIGNMENT', 'DELIVERY_CONFIRM'),
    is_read BOOLEAN DEFAULT FALSE,
    date_notif TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (id_package) REFERENCES package(id_package),
    FOREIGN KEY (id_user_target) REFERENCES users(id)
);


--@block
---- TESTING DATA ----


--@block
INSERT INTO users (email, username, password_hash, first_name, last_name, phone_number, role)
VALUES
('admin@example.com', 'admin', 'hashed_password_here', 'Admin', 'User', '1234567890', 'ADMIN'),
('client1@example.com', 'client1', 'hashed_password_here', 'Alice', 'Smith', '1112223333', 'CLIENT'),
('deliverer1@example.com', 'deliverer1', 'hashed_password_here', 'Bob', 'Johnson', '4445556666', 'DELIVERER');

--@block
SELECT * FROM users;


--@block
INSERT INTO package 
(id_client_source, id_client_destination, vehicle_type_needed, address_source, address_destination, weight, price)
VALUES
(17, 18, 'CAR', 'A', 'B', 1.0, 20.0);


--@block
INSERT INTO users (email, username, password_hash, first_name, last_name, phone_number, role)
VALUES ('c1@mail.com', 'client1', 'pass', 'John', 'Doe', '123456', 'CLIENT');
--@block
INSERT INTO client (id, address) VALUES (LAST_INSERT_ID(), '123 Main St');

--@block
INSERT INTO users (email, username, password_hash, first_name, last_name, phone_number, role)
VALUES ('c2@mail.com', 'client2', 'pass', 'Jane', 'Doe', '789123', 'CLIENT');
--@block
INSERT INTO client (id, address)
VALUES (LAST_INSERT_ID(), 'Address 2');

--@block
DELETE FROM users WHERE username = 'client1';
DELETE FROM users WHERE username = 'client2';

--@block
SELECT * FROM users;
SELECT * FROM client;
SELECT * FROM deliverer;

--@block
SELECT * FROM package;


--@block
INSERT INTO deliverer (id, vehicle_type, is_available)
VALUES (3, 'BIKE', TRUE);
--@block
SELECT * FROM package;


--@block
ALTER TABLE affectation 
MODIFY COLUMN status ENUM('PENDING', 'ACCEPTED', 'REJECTED', 'COMPLETED') NOT NULL;

--@block add column max_weight to deliverer
ALTER TABLE deliverer 
ADD COLUMN max_weight FLOAT NOT NULL;


--@block 
SELECT * FROM deliverer;