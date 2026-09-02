# 📋 Online Complaint Management System (Java Web Portal)

A zero-dependency, lightweight Java web application for managing support ticket lifecycles, triaging service issues, and handling role-based complaint workflows through an interactive web dashboard.

---

## 📌 Problem Statement & Overview
Traditional manual complaint handling (via physical forms, emails, or spreadsheets) leads to unorganized tracking, poor operational accountability, and delayed issue resolutions.

This system provides a centralized digital portal for:
* **Users / Students:** Submitting detailed complaints, selecting categories/priorities, and tracking real-time status updates.
* **Administrators:** Reviewing submitted tickets, assigning dedicated staff members, updating resolution states, and closing completed issues.

---

## 🎯 Key Features
* **Interactive Web UI:** Clean HTML5/CSS3 dashboard powered directly by standard Java HTTP engines.
* **Role-Based Workflows:** Enforces distinct capabilities for `USER` and `ADMIN` roles.
* **Lifecycle State Machine:** Manages ticket state transitions: `OPEN` → `IN_PROGRESS` → `RESOLVED` → `CLOSED`.
* **Dynamic Ticket Actions:** Live administrative controls for instant assignment, resolution, and closure.
* **Thread-Safe Memory Layer:** Leverages `ConcurrentHashMap` and `AtomicLong` for process execution and multi-user safety.

---

## 🛠️ Tech Stack
* **Core Language:** Java (JDK 17+)
* **HTTP Engine:** `com.sun.net.httpserver.HttpServer` (Native Java Library — Zero external dependencies)
* **Architecture:** In-Memory Data Models, Object-Oriented Design (Records, Enums, Concurrent Collections)
* **Build Tooling:** Standard Java Compiler (`javac`) / Maven-compatible structure

---

## 🏛️ System Architecture & Workflow

```text
  [ User Submits Ticket ]
             │
             ▼
      ┌──────────────┐
      │ Status: OPEN │
      └──────┬───────┘
             │ (Admin Assigns Staff)
             ▼
 ┌──────────────────────┐
 │ Status: IN_PROGRESS  │
 └───────────┬──────────┘
             │ (Admin Resolves Issue)
             ▼
   ┌──────────────────┐
   │ Status: RESOLVED │
   └─────────┬────────┘
             │ (User/Admin Closes Ticket)
             ▼
    ┌────────────────┐
    │ Status: CLOSED │
    └────────────────┘
