CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    username VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(30),
    email_verified BOOLEAN DEFAULT FALSE,
    role ENUM('ADMIN', 'CLIENT', 'DELIVERER') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE client (
    id INT PRIMARY KEY,
    address VARCHAR(255),
    city VARCHAR(100),
    postal_code INT,
    FOREIGN KEY (id) REFERENCES users(id) ON DELETE CASCADE
);


CREATE TABLE deliverer (
    id INT PRIMARY KEY,
    vehicle_type ENUM('CAR', 'BIKE', 'TRUCK') NOT NULL,
    max_weight FLOAT NOT NULL,
    current_load FLOAT DEFAULT 0,
    serial_number VARCHAR(100) UNIQUE,
    city VARCHAR(100),
    rate FLOAT DEFAULT 0,
    is_available BOOLEAN DEFAULT TRUE,
    is_approved BOOLEAN DEFAULT FALSE,
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

    FOREIGN KEY (id_client_source) REFERENCES client(id) ON DELETE CASCADE,
    FOREIGN KEY (id_client_destination) REFERENCES client(id) ON DELETE SET NULL
);

CREATE TABLE affectation (
    id_affectation INT AUTO_INCREMENT PRIMARY KEY,
    id_deliverer INT NOT NULL,
    id_package INT NOT NULL,
    status ENUM('PENDING', 'ACCEPTED','ONROUTE', 'REJECTED', 'COMPLETED') NOT NULL,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (id_deliverer) REFERENCES deliverer(id) ON DELETE CASCADE,
    FOREIGN KEY (id_package) REFERENCES package(id_package) ON DELETE CASCADE
);


CREATE TABLE notification (
    id_notification INT AUTO_INCREMENT PRIMARY KEY,
    id_package INT NOT NULL,
    id_user_target INT NOT NULL,
    message VARCHAR(255),
    type ENUM('STATUS_UPDATE', 'ASSIGNMENT', 'DELIVERY_CONFIRM'),
    is_read BOOLEAN DEFAULT FALSE,
    date_notif TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (id_package) REFERENCES package(id_package) ON DELETE CASCADE,
    FOREIGN KEY (id_user_target) REFERENCES users(id) ON DELETE CASCADE
);


CREATE TABLE email_verification (
    user_id INT PRIMARY KEY,
    verification_code_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
ALTER TABLE users ADD COLUMN email_verified BOOLEAN DEFAULT FALSE;
ALTER TABLE client ADD COLUMN city VARCHAR(100);
ALTER TABLE client ADD COLUMN postal_code INT;
CREATE TABLE email_verification (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    verification_code VARCHAR(6) NOT NULL,
    expiration_time DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

DELETE FROM users WHERE email = 'walidchemat@gmail.com';
ALTER TABLE email_verification 
CHANGE verification_code verification_code_hash VARCHAR(255) NOT NULL;



-- 1. Drop the bad table if it exists
DROP TABLE IF EXISTS email_verification;

-- 2. Create the correct table with ALL required columns
CREATE TABLE email_verification (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    verification_code_hash VARCHAR(255) NOT NULL, -- Fixed: Matches Java's "Hash" expectation
    expiration_time DATETIME NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Fixed: The missing column causing your error
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 1. Drop the bad table if it exists
DROP TABLE IF EXISTS email_verification;

-- 2. Create the correct table with ALL required columns
CREATE TABLE email_verification (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    verification_code_hash VARCHAR(255) NOT NULL, -- Fixed: Matches Java's "Hash" expectation
    expiration_time DATETIME NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Fixed: The missing column causing your error
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

DELETE FROM users WHERE email = 'hanadjib70@gmail.com';
-- 1. Wipe the mismatching table
DROP TABLE IF EXISTS email_verification;

-- 2. Create the PERFECT table matching your Java code
CREATE TABLE email_verification (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    verification_code_hash VARCHAR(255) NOT NULL,
    expires_at DATETIME NOT NULL,                   -- Fixed: Renamed from 'expiration_time' to match Java
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Fixed: Java asked for this earlier
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
