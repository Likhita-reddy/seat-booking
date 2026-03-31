-- -----------------------------------------------
-- users-service owns this
-- -----------------------------------------------
CREATE DATABASE IF NOT EXISTS users_db;
USE users_db;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- -----------------------------------------------
-- booking-service owns this
-- -----------------------------------------------
CREATE DATABASE IF NOT EXISTS booking_db;
USE booking_db;

CREATE TABLE IF NOT EXISTS shows (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    venue VARCHAR(200) NOT NULL,
    show_date TIMESTAMP NOT NULL,
    total_seats INT NOT NULL,
    available_seats INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version INT NULL DEFAULT 0
    );

CREATE TABLE IF NOT EXISTS seat_categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    show_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    total_seats INT NOT NULL,
    available_seats INT NOT NULL,
    version INT NOT NULL DEFAULT 0,
    FOREIGN KEY (show_id) REFERENCES shows(id)
    );

CREATE TABLE IF NOT EXISTS seats (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    show_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    seat_number VARCHAR(20) NOT NULL,
    status VARCHAR(20) DEFAULT 'AVAILABLE',
    version INT NOT NULL DEFAULT 0,
    FOREIGN KEY (show_id) REFERENCES shows(id),
    FOREIGN KEY (category_id) REFERENCES seat_categories(id)
    );

CREATE TABLE IF NOT EXISTS bookings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    show_id BIGINT NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    FOREIGN KEY (show_id) REFERENCES shows(id)
    );

CREATE TABLE IF NOT EXISTS booked_seats (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    seat_id BIGINT NOT NULL,
    price_at_booking DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (booking_id) REFERENCES bookings(id),
    FOREIGN KEY (seat_id) REFERENCES seats(id)
    );

-- -----------------------------------------------
-- payment-service owns this
-- -----------------------------------------------
CREATE DATABASE IF NOT EXISTS payment_db;
USE payment_db;

CREATE TABLE IF NOT EXISTS payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- -----------------------------------------------
-- notification-service owns this
-- -----------------------------------------------
CREATE DATABASE IF NOT EXISTS notification_db;
USE notification_db;

CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(20) DEFAULT 'EMAIL',
    status VARCHAR(20) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- -----------------------------------------------
-- Seed data
-- -----------------------------------------------
USE booking_db;

INSERT INTO shows (name, venue, show_date, total_seats, available_seats)
VALUES
    ('IPL Final', 'Wankhede Stadium', '2025-05-01 19:00:00', 10, 10),
    ('Concert Night', 'HICC Hyderabad', '2025-06-15 20:00:00', 6, 6);

INSERT INTO seat_categories (show_id, name, price, total_seats, available_seats) VALUES
                                                                                     (1, 'VIP',     5000.00, 2, 2),
                                                                                     (1, 'Premium', 2000.00, 3, 3),
                                                                                     (1, 'General',  500.00, 5, 5);
INSERT INTO seat_categories (show_id, name, price, total_seats, available_seats) VALUES
                                                                                     (2, 'VIP',     3000.00, 2, 2),
                                                                                     (2, 'General',  800.00, 4, 4);

INSERT INTO seats (show_id, category_id, seat_number, status) VALUES
                                                                  (1, 1, 'V1', 'AVAILABLE'),
                                                                  (1, 1, 'V2', 'AVAILABLE'),
                                                                  (1, 2, 'P1', 'AVAILABLE'),
                                                                  (1, 2, 'P2', 'AVAILABLE'),
                                                                  (1, 2, 'P3', 'AVAILABLE'),
                                                                  (1, 3, 'G1', 'AVAILABLE'),
                                                                  (1, 3, 'G2', 'AVAILABLE'),
                                                                  (1, 3, 'G3', 'AVAILABLE'),
                                                                  (1, 3, 'G4', 'AVAILABLE'),
                                                                  (1, 3, 'G5', 'AVAILABLE');

INSERT INTO seats (show_id, category_id, seat_number, status) VALUES
                                                                  (2, 4, 'V1', 'AVAILABLE'),
                                                                  (2, 4, 'V2', 'AVAILABLE'),
                                                                  (2, 5, 'G1', 'AVAILABLE'),
                                                                  (2, 5, 'G2', 'AVAILABLE'),
                                                                  (2, 5, 'G3', 'AVAILABLE'),
                                                                  (2, 5, 'G4', 'AVAILABLE');