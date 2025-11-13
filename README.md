# 🚗 Car Parking Management System - Interview Assessment

[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://www.oracle.com/java/)
[![Maven](https://img.shields.io/badge/Maven-3.8+-blue.svg)](https://maven.apache.org/)
[![JUnit](https://img.shields.io/badge/JUnit-5.10.1-green.svg)](https://junit.org/junit5/)
[![Mockito](https://img.shields.io/badge/Mockito-5.8.0-red.svg)](https://site.mockito.org/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

## 🎯 Project Overview

This is a **hands-on coding assessment** for evaluating candidates on their ability to write effective unit and integration tests using **Mockito** and **JUnit 5**.

### What We're Testing

#### ✅ Test1
- **Focus**: Unit testing fundamentals with Mockito
- **Skills**: Mocking, verification, exception handling, ArgumentCaptor

#### ✅ Test2
- **Focus**: Integration testing with complex scenarios
- **Skills**: All Test1 skills + concurrency, rollback handling, multi-service coordination

---

## 🏗️ System Architecture

This parking management system includes:

- **Vehicle Management**: Support for Motorcycle, Car, Van, Bus
- **Parking Operations**: Entry, exit, payment processing
- **Reservation System**: Advanced booking with conflict detection
- **Pricing Engine**: Dynamic pricing based on vehicle type and duration
- **Payment Gateway**: Simulated payment processing
- **Concurrency Control**: Thread-safe operations

---

## 🚀 Getting Started

### Prerequisites

- **Java 17** or higher
- **Maven 3.8** or higher
- IDE: IntelliJ IDEA, Eclipse, or VS Code

### Installation
```bash
# Clone the repository
git clone https://github.com/amit-hembrom/car-parking.git
cd car-parking

# Build the project (this will fail tests initially - expected!)
mvn clean install -DskipTests

# Verify compilation works
mvn clean compile
# Expected output: BUILD SUCCESS
``` 
<hr></hr>
📝 Assignment Details
Test 1: ParkingServiceTest1 (Foundational Tests)

File: src/test/java/com/parking/firstUseCase/ParkingServiceTest1.java

Overview
These are pre-built foundational tests that validate basic parking system functionality. Candidates should understand how these work as they provide examples of the system's expected behavior.


Tests Included (4 complete tests)

✅ testParkVehicle - Basic vehicle parking

✅ testExitVehicle - Vehicle exit and fee calculation

✅ testParkingLotFull - Full parking lot scenario

✅ testCreateReservation - Reservation creation

Note: These tests are already implemented and should pass once Test 2 is properly coded.


Test 2: ParkingServiceTest2 (Concurrency & Thread Safety)

File: src/test/java/com/parking/secondUseCase/ParkingServiceTest2.java

Overview

Advanced tests that evaluate concurrent programming skills and thread safety implementation. These tests intentionally fail until candidates implement proper synchronization.

What Candidates Must Implement in ParkingService
🔧 Core Methods (Provide signatures only, remove implementations)
```bash
public ParkingTicket parkVehicle(Vehicle vehicle) {
    // TODO: Implement thread-safe parking logic with atomic spot allocation
}

public double exitVehicle(String ticketId) {
    // TODO: Implement thread-safe exit processing with proper cleanup
}

public Reservation createReservation(String userId, Vehicle vehicle, 
                                     LocalDateTime startTime, LocalDateTime endTime) {
    // TODO: Implement reservation logic with concurrency support
}

private ParkingSpot findAndReserveAvailableSpot() {
    // TODO: Critical - implement atomic spot allocation to prevent race conditions
}
```

📋 Missing Methods (Add these signatures, verify first if they are needed)

```bash

/**
 * Returns list of all active (unprocessed) parking tickets
 */
public List<ParkingTicket> getActiveTickets() {
    // TODO: Return all unprocessed parking tickets
}

/**
 * Returns list of all reservations in the system
 */
public List<Reservation> getAllReservations() {
    // TODO: Return all reservations in the system
}

/**
 * Activates a reservation when time window begins
 */
public void activateReservation(String reservationId) {
    // TODO: Activate reservation when time window begins
}

/**
 * Completes a reservation and frees the spot
 */
public void completeReservation(String reservationId) {
    // TODO: Complete reservation and free the spot
}

```

🧪 Running Tests

```bash
# Run all tests (will have failures initially)
mvn test

# Run Test 1 only
mvn test -Dtest=ParkingServiceTest1

# Run Test 2 only
mvn test -Dtest=ParkingServiceTest2

# Run a specific test method
mvn test -Dtest=ParkingServiceTest2#testHighContentionConcurrentParking

```

🎯 Evaluation Criteria
We're assessing:

✅ Concurrency Skills - Thread-safe implementations

✅ System Design - Architectural decisions

✅ Code Quality - Clean, maintainable code

✅ Problem Solving - Handling race conditions and edge cases


📌 Important Notes

Test 2 failures are intentional and expected! The goal is to implement proper synchronization to make these challenging tests pass.


🎉 Good Luck!
May your threads never deadlock! ⚡


