````markdown
# Cloud of Clouds

## Intelligent Multi-Cloud File Storage System

Cloud of Clouds is a secure multi-cloud file storage system designed to store, encrypt, replicate, and manage files across multiple cloud storage providers.

The system integrates **Amazon S3, Google Cloud Storage, and Microsoft Azure Blob Storage** and uses an adaptive cloud-selection mechanism based on cloud health, latency, and historical runtime performance.

Files are encrypted before cloud storage, replicated to a secondary provider, and can be recovered from the replica when the primary provider is unavailable.

---

## Key Features

- User registration and login
- JWT-based authentication
- BCrypt password hashing
- Protected REST APIs
- Multi-cloud storage
- AWS S3 integration
- Google Cloud Storage integration
- Microsoft Azure Blob Storage integration
- AES-256-GCM file encryption
- Adaptive cloud selection
- Cloud health monitoring
- Latency measurement
- Historical performance learning
- Primary and replica storage
- Primary-cloud failover
- File upload, listing, download, and deletion
- React-based web interface
- Environment-based secret management

---

# System Architecture

```text
                         React Frontend
                               |
                               v
                     Spring Boot REST API
                               |
              +----------------+----------------+
              |                |                |
              v                v                v
        Authentication     PostgreSQL     Adaptive Cloud
        & Authorization      Database        Selection
                                               |
                         +---------------------+---------------------+
                         |                     |                     |
                         v                     v                     v
                      AWS S3                 GCP                  Azure
                         |                     |                     |
                         +---------------------+---------------------+
                                               |
                                        Replica Storage
````

---

# Technology Stack

## Frontend

* React
* TypeScript
* Vite
* HTML
* CSS

## Backend

* Java
* Spring Boot
* Spring Web
* Spring Data JPA
* Spring Security
* JWT
* BCrypt
* Maven

## Database

* PostgreSQL

## Cloud Storage

* Amazon S3
* Google Cloud Storage
* Microsoft Azure Blob Storage
* Local storage for development

## Security

* JWT authentication
* BCrypt password hashing
* AES-256-GCM encryption
* Protected REST APIs
* Environment-based secrets
* Cloud credentials outside source code

---

# Architecture Components

## 1. User Authentication

The system provides:

* User registration
* User login
* Password hashing using BCrypt
* JWT token generation
* JWT-based request authentication
* Protected file-management APIs

Authentication flow:

```text
User
  |
  v
Register
  |
  v
Password hashed using BCrypt
  |
  v
PostgreSQL
  |
  v
Login
  |
  v
JWT Token
  |
  v
Protected APIs
```

---

# 2. Multi-Cloud Storage Abstraction

The application uses a common `StorageProvider` abstraction so that file-management logic does not depend directly on a specific cloud provider.

Supported providers:

```text
StorageProvider
      |
      +---- AWS S3
      |
      +---- Google Cloud Storage
      |
      +---- Azure Blob Storage
      |
      +---- Local Storage
```

This allows the application to select different providers without changing the main file-management workflow.

---

# 3. Adaptive Cloud Selection

The system evaluates available cloud providers using runtime observations such as:

* Cloud health
* Current latency
* Historical performance

The adaptive scoring mechanism combines these measurements to determine an appropriate primary storage provider.

The scoring mechanism uses:

```text
Current Health
      +
Current Latency
      +
Historical Performance
      |
      v
Adaptive Cloud Score
      |
      v
Primary Cloud Selection
```

Historical performance is updated using runtime observations so that the system can learn from previous provider performance.

The implementation is an **explainable adaptive scoring mechanism**, rather than a neural-network or deep-learning model.

---

# 4. File Replication

Each newly uploaded file is stored using:

```text
                    File Upload
                         |
                         v
                Adaptive Selection
                         |
                +--------+--------+
                |                 |
                v                 v
          Primary Cloud      Replica Cloud
                |                 |
                +--------+--------+
                         |
                         v
                  File Metadata
                    PostgreSQL
```

The current configuration uses:

```text
Replica Provider = AWS S3
```

The primary provider can be selected dynamically from the available eligible cloud providers.

The database records:

* Primary provider
* Replica provider
* Original filename
* Stored filename
* File size
* Content type
* User
* Upload information

---

# 5. AES-256-GCM Encryption

Files are encrypted before being uploaded to cloud storage.

Encryption flow:

```text
Original File
     |
     v
AES-256-GCM Encryption
     |
     v
Encrypted Data
     |
     +------------+
     |            |
     v            v
Primary Cloud   Replica Cloud
```

During download:

```text
Primary / Replica Cloud
          |
          v
   Encrypted Data
          |
          v
   AES-256-GCM Decryption
          |
          v
    Original File
```

The encryption key is supplied through:

```text
CLOUD_ENCRYPTION_KEY
```

The encryption key is not stored directly in the source code.

---

# 6. Primary Cloud Failover

When a file is downloaded, the application first attempts to retrieve it from the configured primary provider.

If the primary provider is unavailable, the application attempts to retrieve the encrypted file from the replica provider.

```text
Download Request
       |
       v
Try Primary Cloud
       |
    +--+--+
    |     |
 Success  Failure
    |       |
    |       v
    |   Try Replica
    |       |
    +-------+
       |
       v
Encrypted Data
       |
       v
Decrypt
       |
       v
Return File
```

This provides application-level failover between the primary and replica storage providers.

---

# 7. Cloud Health Monitoring

The backend provides health and performance information for the configured storage providers.

Health checks evaluate provider availability and measure response latency.

Available endpoints include:

```text
GET /api/health/aws
GET /api/health/gcp
GET /api/health/azure
GET /api/health/local
GET /api/health/status
GET /api/health/best
GET /api/health/scores
GET /api/health/historical
GET /api/health/analysis
```

The frontend uses this information to display:

* Provider health
* Latency
* Adaptive scores
* Selected cloud
* Historical performance

---

# 8. File Management

Authenticated users can:

* Upload files
* View stored files
* Download files
* Delete files

The frontend displays:

* File name
* File size
* Primary cloud
* Replica cloud
* Download action
* Delete action

The delete operation includes a confirmation dialog to prevent accidental deletion.

---

# API Endpoints

## Authentication

```text
POST /api/auth/register
POST /api/auth/login
```

## File Management

```text
POST   /api/files/upload
GET    /api/files
GET    /api/files/{id}/download
DELETE /api/files/{id}
```

## Cloud Health

```text
GET /api/health/aws
GET /api/health/gcp
GET /api/health/azure
GET /api/health/local
GET /api/health/status
GET /api/health/best
GET /api/health/scores
GET /api/health/historical
GET /api/health/analysis
```

---

# Project Structure

```text
Cloud of Clouds
│
├── multicloud-storage-api
│   │
│   ├── src
│   │   ├── main
│   │   │   ├── java
│   │   │   │   └── com.multicloud.multicloud_storage_api
│   │   │   │       ├── config
│   │   │   │       ├── controller
│   │   │   │       ├── dto
│   │   │   │       ├── entity
│   │   │   │       ├── repository
│   │   │   │       ├── security
│   │   │   │       └── service
│   │   │   │
│   │   │   └── resources
│   │   │       └── application.properties
│   │   │
│   │   └── test
│   │
│   ├── pom.xml
│   └── mvnw
│
└── multicloud-storage-frontend
    │
    ├── src
    │   ├── App.tsx
    │   ├── App.css
    │   ├── Login.tsx
    │   ├── Register.tsx
    │   ├── main.tsx
    │   └── index.css
    │
    ├── package.json
    └── vite.config.ts
```

---

# Configuration and Secrets

Sensitive configuration is supplied through environment variables.

The application uses variables such as:

```text
DB_PASSWORD
JWT_SECRET
CLOUD_ENCRYPTION_KEY
AZURE_STORAGE_CONNECTION_STRING
```

Cloud credentials are not hardcoded into the application source code.

Credential files, environment files, build output, and other sensitive/generated files are excluded through `.gitignore`.

---

# Database

The application uses PostgreSQL for persistent application data.

The database stores information such as:

```text
Users
Files
File metadata
Primary provider
Replica provider
Upload information
```

The Spring Boot application uses:

```text
Spring Data JPA
Hibernate
PostgreSQL JDBC Driver
```

---

# Running the Backend

Navigate to the backend directory:

```bash
cd multicloud-storage-api/multicloud-storage-api
```

Build the application:

```bash
./mvnw clean package -DskipTests
```

Run the generated JAR:

```bash
java -jar target/multicloud-storage-api-0.0.1-SNAPSHOT.jar
```

The backend runs on:

```text
http://localhost:8080
```

---

# Running the Frontend

Navigate to the frontend directory:

```bash
cd multicloud-storage-frontend
```

Install dependencies:

```bash
npm install
```

Start the development server:

```bash
npm run dev
```

The frontend runs on:

```text
http://localhost:5173
```

For a production build:

```bash
npm run build
```

---

# Testing and Verification

The system has been tested for:

* User registration
* User login
* JWT authentication
* Protected APIs
* File upload
* File listing
* File download
* File deletion
* Delete confirmation
* AES-256-GCM encryption
* AWS S3 storage
* Google Cloud Storage
* Azure Blob Storage
* Cloud health monitoring
* Adaptive cloud selection
* Historical adaptive scoring
* Primary/replica storage
* Primary-cloud failover
* Frontend authentication
* Frontend file management
* Production frontend build
* Backend JAR packaging

A final end-to-end regression test verified:

```text
Login
  ↓
Upload
  ↓
Primary Cloud Selection
  ↓
Replica Storage
  ↓
Download
  ↓
Decryption
  ↓
Original Content Verification
  ↓
Delete
```

---

# Build Verification

## Backend

```bash
./mvnw clean package -DskipTests
```

The Spring Boot executable JAR was successfully generated.

## Frontend

```bash
npm run build
```

The Vite production build completed successfully.

---

# Project Status

The core **Cloud of Clouds MVP is implemented and operational**.

The current system provides:

* Secure user authentication
* JWT authorization
* PostgreSQL persistence
* AES-256-GCM file encryption
* Multi-cloud storage
* AWS S3 integration
* Google Cloud Storage integration
* Azure Blob Storage integration
* Adaptive cloud selection
* Cloud health monitoring
* Historical performance learning
* Primary and replica storage
* Primary-cloud failover
* React-based file management

The application has been tested through both backend API testing and the React frontend.

---

# Future Enhancements

Possible future enhancements include:

* Machine-learning-based cloud prediction
* More advanced cost-aware provider selection
* Automatic background health monitoring
* Larger-scale replication strategies
* Chunk-based file storage
* Kubernetes deployment
* Kafka-based event processing
* Additional cloud providers
* Automated CI/CD deployment
* More comprehensive automated test coverage

````

