-- Disable foreign key checks temporarily
SET FOREIGN_KEY_CHECKS = 0;

-- Recreate tables if needed (customize this based on your actual entity structure)
-- This is just a template, adjust according to your actual data model

-- Create customer table
CREATE TABLE IF NOT EXISTS customer (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255),
    email VARCHAR(255)
    -- add other columns as needed
);

-- Create bank_account table
CREATE TABLE IF NOT EXISTS bank_account (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    balance DECIMAL(19,2),
    created_at DATETIME,
    customer_id BIGINT,
    -- add other columns as needed
    INDEX (customer_id)
);

-- Create account_operation table
CREATE TABLE IF NOT EXISTS account_operation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    amount DECIMAL(19,2),
    operation_date DATETIME,
    description VARCHAR(255),
    bank_account_id BIGINT,
    -- add other columns as needed
    INDEX (bank_account_id)
);

-- Create users and roles tables if needed
CREATE TABLE IF NOT EXISTS app_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(255) UNIQUE,
    password VARCHAR(255)
    -- add other columns as needed
);

CREATE TABLE IF NOT EXISTS app_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_name VARCHAR(255) UNIQUE
    -- add other columns as needed
);

CREATE TABLE IF NOT EXISTS users_roles (
    user_id BIGINT,
    role_id BIGINT,
    PRIMARY KEY (user_id, role_id)
);

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;
