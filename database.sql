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
    city VARCHAR(100),
    postal_code INT,
    phone_verified BOOLEAN DEFAULT FALSE,
    email_verified BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (id) REFERENCES users(id) ON DELETE CASCADE
);


CREATE TABLE deliverer (
    id INT PRIMARY KEY,
    vehicle_type ENUM('CAR', 'BIKE', 'TRUCK') NOT NULL,
    max_weight FLOAT NOT NULL,
    city VARCHAR(100),
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
    delivery_instructions TEXT,
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
