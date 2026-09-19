# Cloud of Clouds

## Intelligent Multi-Cloud File Storage System

Cloud of Clouds is a secure multi-cloud file storage system built with Spring Boot, React, PostgreSQL, and multiple cloud storage providers.

The system automatically evaluates cloud health and performance, selects an appropriate primary cloud for file storage, replicates files to a secondary cloud, encrypts file contents before storage, and provides failover when the primary cloud becomes unavailable.

## Architecture

```text
React Frontend
       |
       v
Spring Boot REST API
       |
       +-------------------+
       |                   |
       v                   v
   PostgreSQL        Adaptive Cloud Selection
                           |
              +------------+------------+
              |            |            |
              v            v            v
           AWS S3        GCP          Azure
              |
              +------ Replica Storage

Technology Stack:

Frontend:
React
TypeScript
Vite
HTML/CSS

Backend:
Java
Spring Boot
Spring Data JPA
Spring Security
JWT Authentication
BCrypt Password Hashing
Maven

Database:
PostgreSQL

Cloud Storage:
Amazon S3
Google Cloud Storage
Microsoft Azure Blob Storage

Security:
JWT authentication
BCrypt password hashing
AES-256-GCM file encryption
Protected REST APIs
Environment-based secrets

Core Features:
1. User Authentication
User registration
Login
JWT authentication
Protected file APIs
Password hashing using BCrypt

2. Multi-Cloud Storage

The system supports:

AWS S3
Google Cloud Storage
Azure Blob Storage
Local storage for development

A common StorageProvider abstraction allows the application to work with different cloud providers without changing the file management logic.

3. Adaptive Cloud Selection

The system evaluates available cloud providers using:

Cloud health
Current latency
Historical performance

The adaptive scoring mechanism combines these factors to select an appropriate primary storage provider.

4. Replication

Uploaded files are stored in:

One dynamically selected primary cloud
One configured replica cloud

Current configuration uses AWS S3 as the replica provider.

5. Encryption

Files are encrypted before being uploaded to cloud storage using:

AES-256-GCM

The encryption key is supplied through an environment variable and is not stored in source code.

6. Failover

If the primary cloud becomes unavailable during download, the system attempts to retrieve the encrypted file from the replica provider.

The downloaded data is then decrypted before being returned to the user.

7. Cloud Health Monitoring

The backend periodically evaluates cloud provider availability and latency.


Available health endpoints include:

GET /api/health/aws
GET /api/health/gcp
GET /api/health/azure
GET /api/health/status
GET /api/health/best
GET /api/health/scores
GET /api/health/historical
GET /api/health/analysis
8. File Management

Authenticated users can:

Upload files
View stored files
Download files
Delete files

The frontend displays:

File name
File size
Primary cloud
Replica cloud
Download action
Delete action

API Endpoints:
Authentication:
POST /api/auth/register
POST /api/auth/login

File Management:
POST   /api/files/upload
GET    /api/files
GET    /api/files/{id}/download
DELETE /api/files/{id} 

Health Monitoring: 

GET /api/health/aws
GET /api/health/gcp
GET /api/health/azure
GET /api/health/status
GET /api/health/best
GET /api/health/scores
GET /api/health/historical
GET /api/health/analysis

Project Structure:

Cloud of Clouds
│
├── multicloud-storage-api
│   ├── src/main/java
│   │   └── com.multicloud.multicloud_storage_api
│   │       ├── config
│   │       ├── controller
│   │       ├── dto
│   │       ├── entity
│   │       ├── repository
│   │       ├── security
│   │       └── service
│   │
│   ├── src/main/resources
│   │   └── application.properties
│   │
│   └── pom.xml
│
└── multicloud-storage-frontend
    ├── src
    │   ├── App.tsx
    │   ├── App.css
    │   ├── Login.tsx
    │   ├── Register.tsx
    │   ├── main.tsx
    │   └── index.css
    │
    └── package.json
Security and Configuration

Sensitive configuration is supplied through environment variables.

Examples include:

DB_PASSWORD
JWT_SECRET
CLOUD_ENCRYPTION_KEY
AZURE_STORAGE_CONNECTION_STRING

Cloud credentials are not hardcoded into the application source code.

Verification

The system has been tested for:

User registration
User login
JWT authentication
File upload
File listing
File download
File deletion
AES-256-GCM encryption
AWS S3 storage
Google Cloud Storage
Azure Blob Storage
Cloud health monitoring
Adaptive cloud selection
Primary/replica storage
Replica failover
Frontend authentication
Frontend file management
Production frontend build

Build:
Backend:
./mvnw clean package -DskipTests

Frontend:
npm run build

Project Status:

The core Cloud of Clouds MVP is implemented and operational.

The system currently provides secure authentication, encrypted multi-cloud storage, adaptive provider selection, replication, health monitoring, and primary-cloud failover through a React frontend and Spring Boot backend.