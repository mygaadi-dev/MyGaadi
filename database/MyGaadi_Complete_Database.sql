/*
===============================================================================
 MyGaadi.com - Complete MySQL Database Script
 Database      : mygaadi
 MySQL Version : 8.x
 Purpose       : Project documentation, fresh database setup and demo data

 This script contains:
   1. Complete database schema
   2. Role profile tables
   3. Authentication, KYC, marketplace, payment, chat and support tables
   4. Soft-delete fields
   5. Demo buyer and seller accounts
   6. Ten demo cars with three bundled local images per car

 Demo credentials created by this script:
   Buyer  : buyer@mygaadi.com  / buyer123
   Seller : seller@mygaadi.com / seller123

 The first ADMIN should be created manually using the commented SQL near the end.
===============================================================================
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE DATABASE IF NOT EXISTS mygaadi;
USE mygaadi;

CREATE TABLE IF NOT EXISTS users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    email VARCHAR(160) NOT NULL UNIQUE,
    phone VARCHAR(20) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('BUYER','SELLER','ADMIN') NOT NULL DEFAULT 'BUYER',
    profile_pic_url TEXT,
    status ENUM('ACTIVE','BLOCKED','PENDING_VERIFICATION') NOT NULL DEFAULT 'ACTIVE',
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    pan_number VARCHAR(10) UNIQUE,
    kyc_holder_name VARCHAR(120),
    kyc_status VARCHAR(30),
    kyc_verified BOOLEAN NOT NULL DEFAULT FALSE,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_users_role (role),
    INDEX idx_users_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


CREATE TABLE IF NOT EXISTS buyer_profiles (
    user_id BIGINT PRIMARY KEY,
    preferred_city VARCHAR(80),
    budget_min BIGINT,
    budget_max BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP NULL,
    CONSTRAINT fk_buyer_profile_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS seller_profiles (
    user_id BIGINT PRIMARY KEY,
    business_name VARCHAR(160),
    pan_number VARCHAR(10),
    kyc_holder_name VARCHAR(120),
    kyc_verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP NULL,
    CONSTRAINT fk_seller_profile_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS admin_profiles (
    user_id BIGINT PRIMARY KEY,
    created_by_admin_id BIGINT NULL,
    department VARCHAR(80),
    permissions TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP NULL,
    CONSTRAINT fk_admin_profile_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_admin_created_by FOREIGN KEY (created_by_admin_id) REFERENCES users(user_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS login_otps (
    otp_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role ENUM('BUYER','SELLER','ADMIN') NOT NULL,
    otp_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    device_info VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_login_otp_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_login_otp_user_role (user_id, role),
    INDEX idx_login_otp_expiry (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- First admin must be inserted manually one time. Password is admin123, change it after first login.
-- INSERT INTO users (name,email,phone,password_hash,role,status,email_verified)
-- VALUES ('Root Admin','admin@mygaadi.com','9000000001','$2a$10$fcutyIZI2kLydByDb4yW.uxlXqaI3kr.o4vAWRG7qrCOZSWoEXy0e','ADMIN','ACTIVE',TRUE);
-- INSERT INTO admin_profiles (user_id, created_by_admin_id, department, permissions)
-- VALUES (LAST_INSERT_ID(), NULL, 'Root Admin', 'ADMIN_PANEL');

CREATE TABLE IF NOT EXISTS refresh_tokens (
    token_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(512) NOT NULL UNIQUE,
    expiry_date TIMESTAMP NOT NULL,
    device_info VARCHAR(255),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_refresh_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_refresh_user (user_id),
    INDEX idx_refresh_expiry (expiry_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS seller_verification (
    verification_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    document_type ENUM('PAN','AADHAAR','PASSPORT','DL') NOT NULL,
    document_number VARCHAR(80) NOT NULL,
    document_url TEXT NOT NULL,
    document_back_url TEXT,
    selfie_url TEXT,
    extra_document_urls JSON,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP NULL,
    status ENUM('PENDING','APPROVED','REJECTED') NOT NULL DEFAULT 'PENDING',
    rejection_reason TEXT,
    submitted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewed_at TIMESTAMP NULL,
    reviewed_by BIGINT NULL,
    CONSTRAINT fk_verification_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_verification_admin FOREIGN KEY (reviewed_by) REFERENCES users(user_id) ON DELETE SET NULL,
    INDEX idx_verification_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS cars (
    car_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    seller_id BIGINT NOT NULL,
    title VARCHAR(180) NOT NULL,
    brand VARCHAR(80) NOT NULL,
    model VARCHAR(80) NOT NULL,
    variant VARCHAR(80),
    year YEAR NOT NULL,
    price DECIMAL(12,2) NOT NULL,
    fuel_type ENUM('PETROL','DIESEL','CNG','ELECTRIC','HYBRID') NOT NULL,
    transmission ENUM('MANUAL','AUTOMATIC','AMT','DCT') NOT NULL,
    mileage_km INT NOT NULL DEFAULT 0,
    engine_cc INT,
    color VARCHAR(40),
    no_of_owners TINYINT NOT NULL DEFAULT 1,
    insurance_valid_till DATE,
    rc_available BOOLEAN NOT NULL DEFAULT TRUE,
    location_city VARCHAR(80) NOT NULL,
    location_state VARCHAR(80) NOT NULL,
    description TEXT,
    status ENUM('ACTIVE','SOLD','PENDING','REMOVED') NOT NULL DEFAULT 'PENDING',
    is_featured BOOLEAN NOT NULL DEFAULT FALSE,
    view_count INT NOT NULL DEFAULT 0,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_car_seller FOREIGN KEY (seller_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_cars_search (brand, model, price, year),
    INDEX idx_cars_status (status),
    INDEX idx_cars_location (location_city, location_state)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS car_images (
    image_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    car_id BIGINT NOT NULL,
    image_url TEXT NOT NULL,
    thumbnail_url TEXT,
    image_type VARCHAR(40),
    display_order TINYINT NOT NULL DEFAULT 0,
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP NULL,
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_image_car FOREIGN KEY (car_id) REFERENCES cars(car_id) ON DELETE CASCADE,
    INDEX idx_images_car (car_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS wishlist (
    wishlist_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    car_id BIGINT NOT NULL,
    added_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP NULL,
    CONSTRAINT fk_wishlist_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_wishlist_car FOREIGN KEY (car_id) REFERENCES cars(car_id) ON DELETE CASCADE,
    UNIQUE KEY uk_wishlist_user_car (user_id, car_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS bookings (
    booking_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    car_id BIGINT NOT NULL,
    buyer_id BIGINT NOT NULL,
    seller_id BIGINT NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    platform_fee DECIMAL(10,2) NOT NULL,
    seller_amount DECIMAL(12,2) NOT NULL,
    booking_status ENUM('INITIATED','ACCEPTED','CONFIRMED','COMPLETED','CANCELLED') NOT NULL DEFAULT 'INITIATED',
    escrow_status ENUM('PENDING','HELD','RELEASED','REFUNDED','DISPUTED') NOT NULL DEFAULT 'PENDING',
    buyer_confirmed BOOLEAN NOT NULL DEFAULT FALSE,
    seller_confirmed BOOLEAN NOT NULL DEFAULT FALSE,
    expiry_at TIMESTAMP NULL,
    notes TEXT,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_booking_car FOREIGN KEY (car_id) REFERENCES cars(car_id),
    CONSTRAINT fk_booking_buyer FOREIGN KEY (buyer_id) REFERENCES users(user_id),
    CONSTRAINT fk_booking_seller FOREIGN KEY (seller_id) REFERENCES users(user_id),
    INDEX idx_booking_buyer (buyer_id),
    INDEX idx_booking_seller (seller_id),
    INDEX idx_booking_status (booking_status, escrow_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS payments (
    payment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    transaction_id VARCHAR(160) NOT NULL UNIQUE,
    razorpay_order_id VARCHAR(100) UNIQUE,
    razorpay_payment_id VARCHAR(100) UNIQUE,
    razorpay_signature VARCHAR(255),
    failure_reason TEXT,
    payment_method ENUM('UPI','CARD','NET_BANKING','WALLET') NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    currency CHAR(3) NOT NULL DEFAULT 'INR',
    status ENUM('PENDING','SUCCESS','FAILED','REFUNDED') NOT NULL DEFAULT 'PENDING',
    gateway_name VARCHAR(80),
    gateway_response JSON,
    paid_at TIMESTAMP NULL,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_payment_booking FOREIGN KEY (booking_id) REFERENCES bookings(booking_id) ON DELETE CASCADE,
    INDEX idx_payment_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS messages (
    message_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sender_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    car_id BIGINT NULL,
    content TEXT NOT NULL,
    message_type ENUM('TEXT','IMAGE','SYSTEM_NOTIFICATION') NOT NULL DEFAULT 'TEXT',
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP NULL,
    sent_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_message_sender FOREIGN KEY (sender_id) REFERENCES users(user_id),
    CONSTRAINT fk_message_receiver FOREIGN KEY (receiver_id) REFERENCES users(user_id),
    CONSTRAINT fk_message_car FOREIGN KEY (car_id) REFERENCES cars(car_id) ON DELETE SET NULL,
    INDEX idx_message_pair (sender_id, receiver_id),
    INDEX idx_message_receiver_read (receiver_id, is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS reviews (
    review_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id BIGINT NOT NULL UNIQUE,
    buyer_id BIGINT NOT NULL,
    seller_id BIGINT NOT NULL,
    rating TINYINT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment TEXT,
    is_flagged BOOLEAN NOT NULL DEFAULT FALSE,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_review_booking FOREIGN KEY (booking_id) REFERENCES bookings(booking_id) ON DELETE CASCADE,
    CONSTRAINT fk_review_buyer FOREIGN KEY (buyer_id) REFERENCES users(user_id),
    CONSTRAINT fk_review_seller FOREIGN KEY (seller_id) REFERENCES users(user_id),
    INDEX idx_review_seller (seller_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS disputes (
    dispute_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    raised_by BIGINT NOT NULL,
    reason TEXT NOT NULL,
    evidence_urls JSON,
    admin_remarks TEXT,
    status ENUM('OPEN','UNDER_REVIEW','RESOLVED','CLOSED') NOT NULL DEFAULT 'OPEN',
    resolution ENUM('REFUND_BUYER','RELEASE_TO_SELLER','PARTIAL') NULL,
    resolved_by BIGINT NULL,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP NULL,
    CONSTRAINT fk_dispute_booking FOREIGN KEY (booking_id) REFERENCES bookings(booking_id),
    CONSTRAINT fk_dispute_user FOREIGN KEY (raised_by) REFERENCES users(user_id),
    CONSTRAINT fk_dispute_admin FOREIGN KEY (resolved_by) REFERENCES users(user_id) ON DELETE SET NULL,
    INDEX idx_dispute_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS reports (
    report_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reporter_id BIGINT NOT NULL,
    reported_user_id BIGINT NULL,
    car_id BIGINT NULL,
    category ENUM('SPAM','FRAUD','INAPPROPRIATE','WRONG_INFO','OTHER') NOT NULL,
    reason TEXT NOT NULL,
    status ENUM('OPEN','UNDER_REVIEW','RESOLVED','DISMISSED') NOT NULL DEFAULT 'OPEN',
    action_taken TEXT,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP NULL,
    CONSTRAINT fk_report_reporter FOREIGN KEY (reporter_id) REFERENCES users(user_id),
    CONSTRAINT fk_report_user FOREIGN KEY (reported_user_id) REFERENCES users(user_id) ON DELETE SET NULL,
    CONSTRAINT fk_report_car FOREIGN KEY (car_id) REFERENCES cars(car_id) ON DELETE SET NULL,
    INDEX idx_report_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS notifications (
    notification_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    notification_type ENUM('BOOKING','PAYMENT','DISPUTE','MESSAGE','REVIEW','SYSTEM') NOT NULL,
    title VARCHAR(160) NOT NULL,
    body TEXT,
    reference_id BIGINT NULL,
    reference_type VARCHAR(50) NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_notification_user_read (user_id, is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

/*
===============================================================================
 DEMO DATA
 This section is idempotent for the demo users and demo cars.
 The bundled images are served by Spring Boot from:
 src/main/resources/static/demo-cars/
===============================================================================
*/

START TRANSACTION;

-- -----------------------------------------------------------------------------
-- Demo buyer
-- BCrypt password: buyer123
-- -----------------------------------------------------------------------------
INSERT INTO users (
    name, email, phone, password_hash, role, status, email_verified,
    kyc_verified, is_deleted, created_at, updated_at
)
SELECT
    'Demo Buyer',
    'buyer@mygaadi.com',
    '9000000002',
    '$2a$10$7tfrM8EC5nZa0hspR4LrIer2BifQS31EjVQnelpOuQK5JVwltTWcm',
    'BUYER', 'ACTIVE', TRUE, FALSE, FALSE, NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE email = 'buyer@mygaadi.com'
);

SET @buyer_id = (
    SELECT user_id FROM users WHERE email = 'buyer@mygaadi.com' LIMIT 1
);

INSERT INTO buyer_profiles (
    user_id, preferred_city, budget_min, budget_max, is_deleted, created_at
)
VALUES (
    @buyer_id, 'Pune', 400000, 1500000, FALSE, NOW()
)
ON DUPLICATE KEY UPDATE
    preferred_city = VALUES(preferred_city),
    budget_min = VALUES(budget_min),
    budget_max = VALUES(budget_max),
    is_deleted = FALSE,
    deleted_at = NULL;

-- -----------------------------------------------------------------------------
-- Demo seller/agent
-- BCrypt password: seller123
-- -----------------------------------------------------------------------------
INSERT INTO users (
    name, email, phone, password_hash, role, status, email_verified,
    pan_number, kyc_holder_name, kyc_status, kyc_verified,
    is_deleted, created_at, updated_at
)
SELECT
    'Verified Seller',
    'seller@mygaadi.com',
    '9000000003',
    '$2a$10$ISsd6XK0CMcpzRCMobiHG.bVBqV.decHK5k3ElcqfnGYYg2gTf5K.',
    'SELLER', 'ACTIVE', TRUE,
    'ABCDE1234F', 'Saitej Shinde', 'COMPLETED', TRUE,
    FALSE, NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE email = 'seller@mygaadi.com'
);

SET @seller_id = (
    SELECT user_id FROM users WHERE email = 'seller@mygaadi.com' LIMIT 1
);

INSERT INTO seller_profiles (
    user_id, business_name, pan_number, kyc_holder_name,
    kyc_verified, is_deleted, created_at
)
VALUES (
    @seller_id, 'MyGaadi Verified Cars', 'ABCDE1234F',
    'Saitej Shinde', TRUE, FALSE, NOW()
)
ON DUPLICATE KEY UPDATE
    business_name = VALUES(business_name),
    pan_number = VALUES(pan_number),
    kyc_holder_name = VALUES(kyc_holder_name),
    kyc_verified = TRUE,
    is_deleted = FALSE,
    deleted_at = NULL;

INSERT INTO seller_verification (
    user_id, document_type, document_number, document_url,
    status, submitted_at, reviewed_at, is_deleted
)
VALUES (
    @seller_id,
    'PAN',
    'ABCDE1234F',
    'http://localhost:8080/demo-cars/kyc/demo-pan-card.jpg',
    'APPROVED',
    NOW(),
    NOW(),
    FALSE
)
ON DUPLICATE KEY UPDATE
    document_type = VALUES(document_type),
    document_number = VALUES(document_number),
    document_url = VALUES(document_url),
    status = 'APPROVED',
    reviewed_at = NOW(),
    rejection_reason = NULL,
    is_deleted = FALSE,
    deleted_at = NULL;

-- -----------------------------------------------------------------------------
-- Temporary seed table for ten cars
-- -----------------------------------------------------------------------------
DROP TEMPORARY TABLE IF EXISTS demo_car_seed;

CREATE TEMPORARY TABLE demo_car_seed (
    title VARCHAR(180) PRIMARY KEY,
    brand VARCHAR(80) NOT NULL,
    model VARCHAR(80) NOT NULL,
    variant VARCHAR(80),
    car_year YEAR NOT NULL,
    price DECIMAL(12,2) NOT NULL,
    fuel_type ENUM('PETROL','DIESEL','CNG','ELECTRIC','HYBRID') NOT NULL,
    transmission ENUM('MANUAL','AUTOMATIC','AMT','DCT') NOT NULL,
    mileage_km INT NOT NULL,
    engine_cc INT,
    color VARCHAR(40),
    no_of_owners TINYINT NOT NULL,
    location_city VARCHAR(80) NOT NULL,
    location_state VARCHAR(80) NOT NULL,
    description TEXT,
    is_featured BOOLEAN NOT NULL,
    image_slug VARCHAR(80) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO demo_car_seed VALUES
('Maruti Suzuki Swift VXI 2020', 'Maruti Suzuki', 'Swift', 'VXI', 2020, 545000.00, 'PETROL', 'MANUAL', 42000, 1197, 'Red', 1, 'Pune', 'Maharashtra', 'Single-owner Swift with a smooth petrol engine, clean cabin and complete service history.', TRUE, 'swift-2020'),
('Hyundai Creta SX Diesel 2021', 'Hyundai', 'Creta', 'SX', 2021, 1285000.00, 'DIESEL', 'MANUAL', 51000, 1493, 'Black', 1, 'Mumbai', 'Maharashtra', 'Well-maintained Creta with premium features, strong diesel performance and excellent highway comfort.', TRUE, 'creta-2021'),
('Honda City VX CVT 2019', 'Honda', 'City', 'VX CVT', 2019, 875000.00, 'PETROL', 'AUTOMATIC', 39000, 1497, 'Silver', 1, 'Nashik', 'Maharashtra', 'Refined automatic sedan with a spacious rear seat, large boot and documented maintenance.', FALSE, 'city-2019'),
('Tata Nexon XZ Plus 2022', 'Tata', 'Nexon', 'XZ Plus', 2022, 995000.00, 'PETROL', 'MANUAL', 26000, 1199, 'Blue', 1, 'Nagpur', 'Maharashtra', 'Low-kilometre compact SUV with high ground clearance, modern infotainment and strong safety package.', TRUE, 'nexon-2022'),
('Mahindra XUV700 AX5 2022', 'Mahindra', 'XUV700', 'AX5', 2022, 1845000.00, 'DIESEL', 'AUTOMATIC', 31000, 2184, 'Red', 1, 'Pune', 'Maharashtra', 'Powerful automatic SUV with premium cabin, connected features and confident road presence.', TRUE, 'xuv700-2022'),
('Toyota Innova Crysta GX 2018', 'Toyota', 'Innova Crysta', 'GX', 2018, 1575000.00, 'DIESEL', 'MANUAL', 76000, 2393, 'Grey', 2, 'Kolhapur', 'Maharashtra', 'Reliable seven-seat family MPV with a durable diesel engine and well-kept interior.', FALSE, 'innova-2018'),
('Kia Seltos HTX 2021', 'Kia', 'Seltos', 'HTX', 2021, 1375000.00, 'PETROL', 'AUTOMATIC', 44000, 1497, 'White', 1, 'Thane', 'Maharashtra', 'Stylish automatic SUV with premium upholstery, touchscreen infotainment and complete paperwork.', TRUE, 'seltos-2021'),
('Maruti Suzuki Baleno Zeta 2020', 'Maruti Suzuki', 'Baleno', 'Zeta', 2020, 635000.00, 'PETROL', 'MANUAL', 36000, 1197, 'Blue', 1, 'Pune', 'Maharashtra', 'Fuel-efficient premium hatchback with low running costs and a clean, practical cabin.', FALSE, 'baleno-2020'),
('MG Hector Sharp DCT 2021', 'MG', 'Hector', 'Sharp DCT', 2021, 1490000.00, 'PETROL', 'DCT', 41000, 1451, 'Black', 1, 'Mumbai', 'Maharashtra', 'Feature-rich SUV with panoramic sunroof, connected-car technology and roomy seats.', FALSE, 'hector-2021'),
('Tata Tiago XZ CNG 2022', 'Tata', 'Tiago', 'XZ CNG', 2022, 595000.00, 'CNG', 'MANUAL', 28000, 1199, 'Orange', 1, 'Aurangabad', 'Maharashtra', 'Economical CNG hatchback ideal for daily commuting, with low mileage and clean condition.', FALSE, 'tiago-2022');

-- Update matching demo cars when the script is run again.
UPDATE cars c
JOIN demo_car_seed d
  ON c.seller_id = @seller_id
 AND c.title = d.title
SET
    c.brand = d.brand,
    c.model = d.model,
    c.variant = d.variant,
    c.`year` = d.car_year,
    c.price = d.price,
    c.fuel_type = d.fuel_type,
    c.transmission = d.transmission,
    c.mileage_km = d.mileage_km,
    c.engine_cc = d.engine_cc,
    c.color = d.color,
    c.no_of_owners = d.no_of_owners,
    c.insurance_valid_till = DATE_ADD(CURDATE(), INTERVAL 10 MONTH),
    c.rc_available = TRUE,
    c.location_city = d.location_city,
    c.location_state = d.location_state,
    c.description = d.description,
    c.status = 'ACTIVE',
    c.is_featured = d.is_featured,
    c.is_deleted = FALSE,
    c.deleted_at = NULL,
    c.updated_at = NOW();

-- Insert only cars not already present for this seller.
INSERT INTO cars (
    seller_id, title, brand, model, variant, `year`, price,
    fuel_type, transmission, mileage_km, engine_cc, color,
    no_of_owners, insurance_valid_till, rc_available,
    location_city, location_state, description, status,
    is_featured, view_count, is_deleted, created_at, updated_at
)
SELECT
    @seller_id,
    d.title,
    d.brand,
    d.model,
    d.variant,
    d.car_year,
    d.price,
    d.fuel_type,
    d.transmission,
    d.mileage_km,
    d.engine_cc,
    d.color,
    d.no_of_owners,
    DATE_ADD(CURDATE(), INTERVAL 10 MONTH),
    TRUE,
    d.location_city,
    d.location_state,
    d.description,
    'ACTIVE',
    d.is_featured,
    0,
    FALSE,
    NOW(),
    NOW()
FROM demo_car_seed d
WHERE NOT EXISTS (
    SELECT 1
    FROM cars c
    WHERE c.seller_id = @seller_id
      AND c.title = d.title
);

-- Replace only the bundled demo images for these cars.
DELETE ci
FROM car_images ci
JOIN cars c ON c.car_id = ci.car_id
JOIN demo_car_seed d ON d.title = c.title
WHERE c.seller_id = @seller_id
  AND ci.image_url LIKE '%/demo-cars/%';

-- Add FRONT, SIDE and REAR bundled images for every demo car.
INSERT INTO car_images (
    car_id, image_url, thumbnail_url, image_type,
    display_order, is_primary, is_deleted, uploaded_at
)
SELECT
    c.car_id,
    CONCAT('http://localhost:8080/demo-cars/', d.image_slug, '/', i.file_name, '.jpg'),
    CONCAT('http://localhost:8080/demo-cars/', d.image_slug, '/', i.file_name, '.jpg'),
    i.image_type,
    i.display_order,
    i.is_primary,
    FALSE,
    NOW()
FROM cars c
JOIN demo_car_seed d ON d.title = c.title
CROSS JOIN (
    SELECT 'front' AS file_name, 'FRONT' AS image_type, 0 AS display_order, TRUE AS is_primary
    UNION ALL
    SELECT 'side', 'SIDE', 1, FALSE
    UNION ALL
    SELECT 'rear', 'REAR', 2, FALSE
) i
WHERE c.seller_id = @seller_id
  AND c.is_deleted = FALSE;

DROP TEMPORARY TABLE IF EXISTS demo_car_seed;

COMMIT;

SET FOREIGN_KEY_CHECKS = 1;

/*
===============================================================================
 FIRST ADMIN - RUN MANUALLY ONCE
 The first admin is intentionally not added automatically.
 Password for the hash below: admin123
 Change the password immediately after first login.
===============================================================================

INSERT INTO users (
    name, email, phone, password_hash, role, status,
    email_verified, is_deleted, created_at, updated_at
)
VALUES (
    'Root Admin',
    'admin@mygaadi.com',
    '9000000001',
    '$2a$10$fcutyIZI2kLydByDb4yW.uxlXqaI3kr.o4vAWRG7qrCOZSWoEXy0e',
    'ADMIN',
    'ACTIVE',
    TRUE,
    FALSE,
    NOW(),
    NOW()
);

SET @root_admin_id = LAST_INSERT_ID();

INSERT INTO admin_profiles (
    user_id, created_by_admin_id, department, permissions,
    is_deleted, created_at
)
VALUES (
    @root_admin_id,
    NULL,
    'Root Administration',
    'ADMIN_PANEL',
    FALSE,
    NOW()
);

===============================================================================
 USEFUL VERIFICATION QUERIES
===============================================================================

SELECT COUNT(*) AS total_users FROM users WHERE is_deleted = FALSE;
SELECT COUNT(*) AS active_cars FROM cars WHERE status = 'ACTIVE' AND is_deleted = FALSE;
SELECT COUNT(*) AS demo_car_images FROM car_images WHERE image_url LIKE '%/demo-cars/%';

SELECT
    c.car_id,
    c.title,
    c.brand,
    c.model,
    c.price,
    c.location_city,
    ci.image_type,
    ci.image_url
FROM cars c
LEFT JOIN car_images ci
       ON ci.car_id = c.car_id
      AND ci.is_deleted = FALSE
WHERE c.seller_id = @seller_id
  AND c.is_deleted = FALSE
ORDER BY c.car_id, ci.display_order;
*/
