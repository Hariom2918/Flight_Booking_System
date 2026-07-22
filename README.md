# ✈️ Flight Booking Management System

A **Java Swing-based Flight Booking Management System** that enables passengers to register, book flights, manage bookings, and allows administrators to manage flight operations. The application uses **JDBC** to connect with a **MySQL** database and provides a simple desktop GUI for seamless interaction. The application verifies database connectivity before launching the interface and supports secure password storage using SHA-256 hashing. :contentReference[oaicite:0]{index=0}

---

## 🚀 Features

### 👨‍✈️ Admin
- Add new flights
- View all available flights
- Remove flights
- View booking statistics
- Secure admin login

### 🧑‍💼 Passenger
- User Registration
- Secure Login
- Book Flights
- View Bookings
- Cancel Bookings

### 🔒 Security
- SHA-256 password hashing
- JDBC Prepared Statements
- Transaction management for booking and cancellation
- Input validation

---

## 🛠️ Tech Stack

- Java
- Java Swing
- JDBC
- MySQL
- SHA-256 Password Hashing

---

## 📂 Project Structure

```
FlightBookingSystem/
│
├── BookingApp.java
├── database.sql
├── README.md
└── lib/
```

---

## 📦 Prerequisites

- Java JDK 17+
- MySQL Server
- MySQL JDBC Connector
- IntelliJ IDEA / Eclipse / NetBeans

---

## ⚙️ Database Setup

1. Create a MySQL database.

```sql
CREATE DATABASE flight_booking;
```

2. Import the SQL schema.

3. Update the database credentials in `BookingApp.java` if required.

```java
DB_URL
DB_USER
DB_PASS
```

---

## ▶️ Running the Project

1. Clone the repository.

```bash
git clone https://github.com/yourusername/flight-booking-system.git
```

2. Open the project in your IDE.

3. Add the MySQL JDBC Driver.

4. Configure your database.

5. Run

```bash
BookingApp.java
```

---

## 📸 Application Workflow

### Passenger

- Register
- Login
- Browse Flights
- Book Flight
- View Bookings
- Cancel Booking

### Admin

- Login
- Add Flights
- View Flights
- Remove Flights
- View Statistics

The application provides separate interfaces for administrators and passengers, each with dedicated functionality for managing flights and bookings. :contentReference[oaicite:1]{index=1}

---

## 💾 Database Tables

- Admins
- Passengers
- Flights
- Bookings

---

## ✨ Future Improvements

- Flight Search Filters
- Payment Gateway Integration
- Email Confirmation
- Ticket PDF Generation
- Seat Selection
- Flight Schedule Management
- Dark Mode UI
- REST API Integration

---

## 🤝 Contributing

Contributions are welcome! Feel free to fork the repository, submit issues, or create pull requests.

---

## 📄 License

This project is licensed under the **MIT License**.

---

## ⭐ Support

If you found this project useful, consider giving it a **⭐ Star** on GitHub!
