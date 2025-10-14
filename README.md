# Wigell Repair Service

WigellRepairService - MicroService \
Developed By: **Simon Jämtgård** \
Java Web Services (Group Project)

[![Java](https://img.shields.io/badge/Java-21-blue.svg)](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.5-brightgreen.svg)](https://spring.io/projects/spring-boot)

### Gateway:
* https://github.com/Jamtgard/WigellGateway.git

### Other Microservices:
* https://github.com/Sommar-skog/WigellTravelService.git
* https://github.com/SaraSnail/WigellGymServices.git
* https://github.com/a-westerberg/WigellPadelService.git

## Overview

- **Language:** Java 21
- **Framework:** Spring Boot 3.5.5
- **Modules:** Spring Web, Spring Data JPA, Validation
- **Database:**
    - **Dev/Test:** H2 (in-memory)
    - **Prod:** MySQL 8 (Docker container)
- **Security:** Spring Security (HTTP Basic, in-memory users)
- **Logging:** Log4j2
- **External API:** Currency conversion (SEK → EUR) via **API Plugin**
- **Profiles:** `dev` (H2), `prod` (MySQL)
- **Port:** Service → `5555`


- **RepairService** — What can be repaired (name, type, price SEK, active, technician)
- **RepairTechnician** — Technicians with a **speciality** (`CAR`, `ELECTRONICS`, `APPLIANCE`)
- **RepairCustomer** — Simple customer registry (name)
- **RepairBooking** — Booking with date, **status** (`BOOKED`, `ON_GOING`, `CANCELED`, `COMPLETED`), and total price (SEK)

Seed data is provided for both **dev** and **prod** profiles.

## Authentication & Roles

HTTP Basic with in-memory users (development placeholders):

| Role  | Username | Password |
|:-----:|:-------:|:--------:|
| ADMIN |  simon  |  simon   |
| USER  |  alex   |   alex   |
| USER  |  sara   |   sara   |
| USER  | amanda  |  amanda  |

## REST API

Base path: `/api/wigellrepairs`

### User
- **GET** `/services` — List active repair services (includes SEK and converted EUR)
- **POST** `/bookservice` — Create a booking
    - Body: `{ "serviceId": Long, "date": "YYYY-MM-DD" }`
- **PUT** `/cancelbooking` — Cancel an existing booking
    - Body: `{ "bookingId": Long }`
- **GET** `/mybookings` — List the authenticated user’s bookings

### Admin
- **GET** `/listcanceled` — List canceled bookings
- **GET** `/listupcoming` — List upcoming bookings
- **GET** `/listpast` — List bookings for past dates
- **POST** `/addservice` — Create new `RepairService`
- **PUT** `/updateservice` — Update `RepairService` (selected fields)
- **PUT** `/removeservice/{id}` — Soft-remove (set inactive) a `RepairService`
- **POST** `/addtechnician` — Create `RepairTechnician`
- **GET** `/technicians` — List active technicians