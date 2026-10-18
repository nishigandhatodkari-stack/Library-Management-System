-- =============================================================================
-- Library Management System - Sample Data SQL Script
-- Passwords below are BCrypt encrypted hashes:
-- 'admin123' -> $2a$10$w6D9cQ73783x9J3b2U3k0.G8uH5aWcWb7pCq8e4u5I.b3jC9Z5Y2i
-- 'librarian123' -> $2a$10$Q7JmQ0rNfZvQhG3.nUvOfehFv5W2aFk6g0gP1/wUa1nQv6iV5f9vC
-- Note: Spring Boot DataInitializer also automatically bootstraps this if the DB is blank.
-- =============================================================================

USE library_db;

-- 1. Default Admin and Librarian Users
INSERT INTO users (username, password, full_name, role, enabled, created_at, updated_at)
VALUES 
('admin', '$2a$10$w6D9cQ73783x9J3b2U3k0.G8uH5aWcWb7pCq8e4u5I.b3jC9Z5Y2i', 'System Administrator', 'ROLE_ADMIN', TRUE, NOW(), NOW()),
('librarian', '$2a$10$Q7JmQ0rNfZvQhG3.nUvOfehFv5W2aFk6g0gP1/wUa1nQv6iV5f9vC', 'Main Campus Librarian', 'ROLE_LIBRARIAN', TRUE, NOW(), NOW())
ON DUPLICATE KEY UPDATE full_name = VALUES(full_name);

-- 2. Sample Books
INSERT INTO books (isbn, title, author, category, publisher, publication_year, total_copies, available_copies, created_at, updated_at)
VALUES
('978-0134685991', 'Effective Java', 'Joshua Bloch', 'Computer Science', 'Addison-Wesley', 2018, 5, 4, NOW(), NOW()),
('978-0132350884', 'Clean Code', 'Robert C. Martin', 'Computer Science', 'Prentice Hall', 2008, 4, 3, NOW(), NOW()),
('978-0201633610', 'Design Patterns', 'Erich Gamma et al.', 'Computer Science', 'Addison-Wesley', 1994, 3, 3, NOW(), NOW()),
('978-0262033848', 'Introduction to Algorithms', 'Thomas H. Cormen', 'Algorithms', 'MIT Press', 2009, 4, 4, NOW(), NOW()),
('978-0073529325', 'Operating System Concepts', 'Abraham Silberschatz', 'Operating Systems', 'Wiley', 2018, 3, 3, NOW(), NOW()),
('978-0321125217', 'Domain-Driven Design', 'Eric Evans', 'Software Architecture', 'Addison-Wesley', 2003, 2, 2, NOW(), NOW())
ON DUPLICATE KEY UPDATE title = VALUES(title);

-- 3. Sample Members
INSERT INTO members (member_id, full_name, email, phone, address, registration_date, status, created_at, updated_at)
VALUES
('MEM-1001', 'Aarav Sharma', 'aarav.sharma@example.com', '+91 9876543210', 'A-12 Tech Enclave, New Delhi', CURDATE() - INTERVAL 60 DAY, 'ACTIVE', NOW(), NOW()),
('MEM-1002', 'Priya Patel', 'priya.patel@example.com', '+91 9812345678', 'Flat 4B, Green Avenue, Bengaluru', CURDATE() - INTERVAL 45 DAY, 'ACTIVE', NOW(), NOW()),
('MEM-1003', 'Rohan Mehta', 'rohan.mehta@example.com', '+91 9923456789', 'Sector 14, Gurugram, Haryana', CURDATE() - INTERVAL 30 DAY, 'ACTIVE', NOW(), NOW()),
('MEM-1004', 'Ananya Iyer', 'ananya.iyer@example.com', '+91 9745612345', 'T-6 Anna Nagar, Chennai', CURDATE() - INTERVAL 15 DAY, 'ACTIVE', NOW(), NOW())
ON DUPLICATE KEY UPDATE full_name = VALUES(full_name);
