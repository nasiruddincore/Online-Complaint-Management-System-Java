# 🎫 Online Complaint Management System (Java Spring Boot)

An industry-ready, RESTful ticket management portal that allows users to log operational complaints and enables administrators to assign, update, and resolve service tickets with clear role-based lifecycle controls.

## 📌 Features
* **Role-Based Authentication:** Custom token-based header authentication (`X-Auth`) supporting `USER` and `ADMIN` roles.
* **Complaint State Machine:** Enforces transition flows: `OPEN` → `IN_PROGRESS` → `RESOLVED` → `CLOSED`.
* **Interactive Commenting:** Multithreaded comment handling for ongoing communication between users and administrators.
* **Advanced Search & Filtering:** Filter complaints by status, operational category (`IT`, `Facility`, etc.), and priority (`Critical`, `High`, etc.).
* **Input Validation:** Strict field-level constraint checks on payload requests.

## 🏗️ Architecture & Data Model
The system uses concurrent, thread-safe memory maps to model users, sessions, tickets, and comment feeds in process, avoiding external infrastructure dependencies for rapid execution and testing.

## 🚀 Getting Started
1. Clone the repository:
   ```bash
   git clone [https://github.com/YOUR_USERNAME/Online-Complaint-Management-System-Java.git](https://github.com/YOUR_USERNAME/Online-Complaint-Management-System-Java.git)
   cd Online-Complaint-Management-System-Java