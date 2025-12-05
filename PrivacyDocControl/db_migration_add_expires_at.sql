-- SQL script to add expires_at column to document table
ALTER TABLE document
ADD COLUMN expires_at TIMESTAMP NULL;
