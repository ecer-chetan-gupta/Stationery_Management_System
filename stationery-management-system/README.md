# Stationery Management System

A microservices-based College Stationery Management System built with:
- **Java 17 + Spring Boot 3.2.5 + Spring Cloud 2023.0.1**
- **React 18 (Vite)** frontend
- **MySQL 8.0** (3 separate databases)
- **Docker + Docker Compose**
- **Jenkins CI/CD**

## Quick Start (Docker)

```bash
docker compose up --build
```

## Local Dev Setup

See Agent.md for the full day-by-day guide.

| Service | Port |
|---|---|
| Eureka | 8761 |
| Config | 8888 |
| Gateway | 8080 |
| Auth | 8081 |
| Inventory | 8082 |
| Requests | 8083 |
| Frontend | 3000 |

> Full README will be completed on Day 5.
