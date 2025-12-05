#PrivacyDocControl

A document management system built for cyber cafés where users can upload documents securely and get a one-time token. Staff can view and print the document once using the token. After printing, the document is deleted automatically.

This project was built to solve a real privacy issue in cyber cafés — users usually share their personal documents through WhatsApp or local PCs, which is risky. This system avoids that.

#Features

Secure document upload

One-time token generation

Staff can view but cannot download

One-time print mechanism

Auto-delete after printing

Session-based access

Basic admin controls (if applicable)

#Tech Stack

Backend: Spring Boot
Frontend (current): Thymeleaf
Frontend (upcoming): React (migration planned)
Database: PostgreSQL
Storage: Local file storage
Security: Cookie-based session + file type validation

#How It Works

User uploads a document

System generates a unique token

User shares the token with café staff

Staff enters token → views document

After printing, document is removed from storage

#Project Structure
backend/
 ├─ src/
frontend-thymeleaf/
docs/
screenshots/   (add later)

#Setup
Backend
mvn clean install
mvn spring-boot:run
