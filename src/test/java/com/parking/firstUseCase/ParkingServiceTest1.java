package com.parking.firstUseCase;

import com.parking.model.*;
import com.parking.service.ParkingService;
import com.parking.service.PricingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class with 6 tests for ParkingService
 */
class ParkingServiceTest1 {

    private ParkingService parkingService;
    private PricingService pricingService;

    @BeforeEach
    void setUp() {
        pricingService = new PricingService();
        parkingService = new ParkingService(pricingService);

        // Add parking spots
        parkingService.addParkingSpot(new ParkingSpot("A1"));
        parkingService.addParkingSpot(new ParkingSpot("A2"));
        parkingService.addParkingSpot(new ParkingSpot("A3"));
    }

    @Test
    @DisplayName("1. Test successful vehicle parking")
    void testParkVehicle() {
        // Arrange
        Vehicle car = new Vehicle("ABC123", VehicleType.CAR);
        // LocalDateTime currentTime = LocalDateTime.now();
        // Act
        ParkingTicket ticket = parkingService.parkVehicle(car);

        // Assert
        assertNotNull(ticket);
        assertNotNull(ticket.getTicketId());
        assertEquals(car, ticket.getVehicle());
        assertNotNull(ticket.getSpot());
        assertTrue(ticket.getSpot().isOccupied());
        assertEquals(car, ticket.getSpot().getParkedVehicle());
        assertFalse(ticket.isProcessed());
    }

    @Test
    @DisplayName("2. Test vehicle exit and fee calculation")
    void testExitVehicle() {
        // Arrange
        Vehicle car = new Vehicle("XYZ789", VehicleType.CAR);
        ParkingTicket ticket = parkingService.parkVehicle(car);
        String ticketId = ticket.getTicketId();

        // Act
        double fee = parkingService.exitVehicle(ticketId);

        // Assert
        assertTrue(fee > 0);
        assertEquals(5.0, fee, 0.01); // CAR_RATE is $5/hour, minimum 1 hour
        assertTrue(ticket.isProcessed());
        assertNotNull(ticket.getExitTime());
        assertFalse(ticket.getSpot().isOccupied());
    }

    @Test
    @DisplayName("3. Test creating a reservation")
    void testCreateReservation() {
        // Arrange
        String userId = "user123";
        Vehicle car = new Vehicle("RES001", VehicleType.CAR);
        LocalDateTime startTime = LocalDateTime.now().plusHours(2);
        LocalDateTime endTime = startTime.plusHours(3);

        // Act
        Reservation reservation = parkingService.createReservation(
                userId, car, startTime, endTime
        );

        // Assert
        assertNotNull(reservation);
        assertNotNull(reservation.getReservationId());
        assertEquals(userId, reservation.getUserId());
        assertEquals(car, reservation.getVehicle());
        assertEquals(ReservationStatus.CONFIRMED, reservation.getStatus());
        assertTrue(reservation.getPaidAmount() > 0);
        // Reservation fee = CAR_RATE * 3 hours * 1.2 premium = 5 * 3 * 1.2 = 18.0
        assertEquals(18.0, reservation.getPaidAmount(), 0.01);
    }


    @Test
    @DisplayName("4. Test parking status reporting")
    void testGetParkingStatus() {
        // Arrange - Park 2 vehicles
        parkingService.parkVehicle(new Vehicle("ST001", VehicleType.CAR));
        parkingService.parkVehicle(new Vehicle("ST002", VehicleType.MOTORCYCLE));

        // Create 1 reservation
        Vehicle car = new Vehicle("ST003", VehicleType.CAR);
        LocalDateTime startTime = LocalDateTime.now().plusHours(1);
        LocalDateTime endTime = startTime.plusHours(2);
        parkingService.createReservation("user789", car, startTime, endTime);

        // Assert
        // Cast to Map first
        Map<String, Object> status = (Map<String, Object>) parkingService.getParkingStatus();

        assertEquals(3L, status.get("totalSpots"));
        assertEquals(2L, status.get("occupiedSpots"));
        assertEquals(1L, status.get("availableSpots"));
        assertEquals(2, status.get("activeTickets"));
        assertEquals(1L, status.get("activeReservations"));
    }
}