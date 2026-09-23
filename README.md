# Tracker Central — Goal & Habit Tracking Platform

A full-stack Spring Boot web application for setting, tracking, and analyzing personal goals across five categories (Study, Exercise, Health, Habit, Finance), with gamified progress tracking, an administrative control panel, and a REST API layer for third-party integration.

![Backend](https://img.shields.io/badge/Backend-Spring%20Boot%204.1-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Security](https://img.shields.io/badge/Security-Spring%20Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)
![UI Engine](https://img.shields.io/badge/UI%20Engine-Thymeleaf-005F0F?style=for-the-badge&logo=thymeleaf&logoColor=white)
![Database](https://img.shields.io/badge/Database-MySQL%208.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![Containerized](https://img.shields.io/badge/Containerized-Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![CI/CD](https://img.shields.io/badge/CI%2FCD-Jenkins-D24939?style=for-the-badge&logo=jenkins&logoColor=white)
![Deployment](https://img.shields.io/badge/Deployment-AWS%20EC2-FF9900?style=for-the-badge&logo=amazonaws&logoColor=white)


**Live Demo:** https://tracker-app-qwm4.onrender.com/login

---

## Overview

Tracker Central was built to solve a common gap in personal productivity tools: most habit trackers either oversimplify progress into a binary checkbox, or overcomplicate it with features unrelated to actual goal completion. This project implements a single, consistent data model for tracking progress toward any measurable goal — whether that's pages read, workouts completed, or money saved — with automatic completion percentage calculation, streak tracking, and milestone-based rewards, backed by a properly normalized relational schema and enforced server-side business rules.

The project was built end-to-end as a portfolio piece demonstrating production-oriented backend engineering practices: layered architecture, authentication and authorization, database schema design and migration management, containerization, and CI/CD pipeline configuration.

---

## Key Features

- **Authentication & Account Security** — registration with email verification, BCrypt password hashing, industry-standard password strength validation, forgot/reset password flow via emailed tokens, and rate-limited login endpoints to mitigate brute-force attempts.
- **Role-Based Access Control** — a three-tier admin hierarchy (primary admin, promoted admins, standard users) where promoted admins can only manage the accounts they personally promoted, enforced at the service layer independent of the UI.
- **Goal & Progress Tracking** — predefined tracker templates across 5 categories plus fully custom trackers, incremental progress logging, automatic completion percentage calculation, and deadline tracking with expiry detection.
- **Gamification Engine** — automatic milestone detection (25/50/75/100% completion, 7-day and 30-day streaks) with duplicate-award prevention, and a corresponding badge system tied to the user account rather than the tracker (so achievements persist even if the originating tracker is later deleted).
- **Notifications** — transactional emails for badge/milestone achievement, goal completion, upcoming deadlines (3-day reminder), and inactivity nudges, sent via a scheduled daily job with duplicate-send protection.
- **Data Export** — CSV and PDF export of a tracker's full progress history.
- **REST API** — a parallel, fully documented (OpenAPI/Swagger) REST layer exposing tracker CRUD, progress logging, and dashboard statistics, with pagination, a consistent response envelope, and resource-ownership enforcement (IDOR protection) independent of the web UI.
- **Admin Panel** — user management (enable/disable/delete/promote) with server-side authorization checks that cannot be bypassed by direct API calls, even if the corresponding UI button is hidden.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 4, Spring MVC, Spring Security, Spring Data JPA |
| Database | MySQL 8, Flyway (schema migrations) |
| Frontend | Thymeleaf, HTML5, CSS3, vanilla JavaScript |
| API Docs | springdoc-openapi (Swagger UI) |
| Email | Spring Mail (SMTP) |
| PDF Generation | OpenPDF |
| Rate Limiting | Bucket4j |
| Build Tool | Maven |
| Containerization | Docker, Docker Compose |
| CI/CD | Jenkins (Declarative Pipeline) |
| Cloud | AWS EC2 |
| Version Control | Git, GitHub |

---

## Architecture

The application follows a standard layered architecture:

Controller (Thymeleaf MVC + REST)
→ Service (interface) / ServiceImpl (business logic)
→ Repository (Spring Data JPA)
→ MySQL


Business logic — streak calculation, milestone detection, completion percentage, ownership validation — lives entirely in the service layer, so it behaves identically whether invoked from the web UI or the REST API.

### Database Schema

The schema consists of 11 core tables: `users`, `roles`, `user_roles`, `trackers`, `progress_entries`, `milestones`, `badges`, `activity_logs`, `verification_tokens`, `password_reset_tokens`, plus Flyway's own migration-history table. Key relationships:

- One user → many trackers (cascading delete)
- One tracker → many progress entries and milestones (cascading delete)
- One user → many badges (independent of tracker lifecycle — badges are not deleted when their originating tracker is)

---

## Security Implementation

- Passwords hashed with BCrypt; never stored or logged in plain text.
- Server-side password policy: minimum 8 characters, mixed case, numeric, special character, common-password rejection, and rejection of passwords containing the user's own name or email.
- CSRF protection enabled on all state-changing web routes.
- IDOR/resource-ownership checks enforced in the service layer for every tracker operation — a user cannot access or modify another user's data by guessing a resource ID, on either the web UI or the REST API.
- Rate limiting (5 requests/minute per IP) on login, password-reset, and verification-resend endpoints.
- Admin actions (disable, delete, promote) are permission-checked server-side, independent of which UI buttons are rendered for a given user.

---

## Running Locally

### Prerequisites
- Java 17
- Maven
- MySQL 8 (or Docker)

### Option 1: Docker Compose (recommended)
```bash
git clone https://github.com/<your-username>/tracker-app.git
cd tracker-app
cp .env.example .env   # fill in your DB password and mail credentials
docker compose up
```
The app will be available at `http://localhost:8080`.

### Option 2: Run directly with Maven
```bash
mvn clean install
mvn spring-boot:run
```
Requires a local MySQL instance and `application.properties` configured with your database and SMTP credentials.

---

## CI/CD Pipeline

A Jenkins Declarative Pipeline automates the build-and-publish process:
1. Builds the application into a Docker image via a multi-stage `Dockerfile` (Maven build stage → minimal JRE runtime stage).
2. Pushes the tagged image to Docker Hub.

See `Jenkinsfile` in the project root.

---

## Deployment

The application is deployed on an AWS EC2 instance running the same Docker Compose configuration used locally, pulling the pre-built image from Docker Hub rather than rebuilding on the server. An Elastic IP provides a stable public address.

---

## Project Structure

src/main/java/com/trackerapp/tracker_app/
├── config/ # Security, scheduling, rate limiting, OpenAPI config
├── controller/ # MVC controllers + REST controllers
├── dto/ # Request/response DTOs
├── entity/ # JPA entities
├── exception/ # Custom exceptions + global exception handling
├── repository/ # Spring Data JPA repositories
├── security/ # Custom UserDetailsService, auth handlers
├── service/ # Service interfaces
├── service/impl/ # Service implementations
└── util/ # Password validation utility

src/main/resources/
├── db/migration/ # Flyway SQL migrations
├── static/ # CSS, JS
└── templates/ # Thymeleaf views


---

## Author

**Shivani S**
