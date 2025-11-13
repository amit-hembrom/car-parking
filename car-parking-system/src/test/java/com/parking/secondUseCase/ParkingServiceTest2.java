package com.parking.secondUseCase;

import com.parking.model.*;
import com.parking.service.ParkingService;
import com.parking.service.PricingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Staff Engineer Level Tests
 *
 * These tests evaluate deep understanding of:
 * - Concurrent programming and thread safety
 * - Race condition prevention
 * - Deadlock avoidance
 * - Data consistency under high contention
 * - System behavior at scale
 */
class ParkingServiceTest2 {

    private ParkingService parkingService;
    private PricingService pricingService;
    private ExecutorService executorService;

    @BeforeEach
    void setUp() {
        pricingService = new PricingService();
        parkingService = new ParkingService(pricingService);
        executorService = Executors.newFixedThreadPool(50);
    }

    @Test
    @DisplayName("Test 2.1: Concurrent High-Contention Parking - No Double Booking")
    void testHighContentionConcurrentParking() throws InterruptedException {
        /*
         * This test validates:
         * 1. No spot is ever double-booked under extreme contention
         * 2. Exactly the right number of operations succeed/fail
         * 3. All successful operations have unique spots
         * 4. System state remains consistent
         * 5. No race conditions in spot allocation
         */

        // Arrange - Create only 10 spots
        int totalSpots = 10;
        for (int i = 1; i <= totalSpots; i++) {
            parkingService.addParkingSpot(new ParkingSpot("SPOT-" + i));
        }

        // We'll have 100 threads compete for 10 spots
        int numberOfThreads = 100;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numberOfThreads);

        // Thread-safe collections to track results
        ConcurrentHashMap<Integer, ParkingTicket> successfulTickets = new ConcurrentHashMap<>();
        ConcurrentHashMap<Integer, Exception> failures = new ConcurrentHashMap<>();

        // Act - All threads attempt to park simultaneously
        for (int i = 0; i < numberOfThreads; i++) {
            final int threadId = i;
            executorService.submit(() -> {
                try {
                    // Wait for all threads to be ready
                    startLatch.await();

                    // Attempt to park
                    Vehicle vehicle = new Vehicle("VEHICLE-" + threadId, VehicleType.CAR);
                    ParkingTicket ticket = parkingService.parkVehicle(vehicle);
                    successfulTickets.put(threadId, ticket);

                } catch (Exception e) {
                    failures.put(threadId, e);
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        // Release all threads at once to maximize contention
        startLatch.countDown();

        // Wait for all operations to complete
        boolean completed = doneLatch.await(30, TimeUnit.SECONDS);
        assertTrue(completed, "All threads should complete within timeout");

        // ============ CRITICAL ASSERTIONS ============

        // 1. Exactly 10 should succeed (matching available spots)
        assertEquals(totalSpots, successfulTickets.size(),
                "Exactly " + totalSpots + " parking operations should succeed");

        // 2. Exactly 90 should fail (remaining threads)
        assertEquals(numberOfThreads - totalSpots, failures.size(),
                "Exactly " + (numberOfThreads - totalSpots) + " operations should fail");

        // 3. All failures should be due to "No available parking spots"
        failures.values().forEach(exception -> {
            assertTrue(exception instanceof IllegalStateException,
                    "All failures should be IllegalStateException");
            assertTrue(exception.getMessage().contains("No available parking spots") ||
                            exception.getMessage().contains("already has an active parking ticket"),
                    "Failure message should indicate no spots or duplicate vehicle");
        });

        // 4. No duplicate spots assigned (CRITICAL - proves no race condition)
        Set<String> assignedSpots = successfulTickets.values().stream()
                .map(ticket -> ticket.getSpot().getSpotId())
                .collect(Collectors.toSet());

        assertEquals(totalSpots, assignedSpots.size(),
                "All " + totalSpots + " different spots should be assigned (no duplicates)");

        // 5. All tickets have unique IDs
        Set<String> ticketIds = successfulTickets.values().stream()
                .map(ParkingTicket::getTicketId)
                .collect(Collectors.toSet());

        assertEquals(totalSpots, ticketIds.size(),
                "All ticket IDs should be unique");

        // 6. Verify each successful ticket has correct state
        successfulTickets.values().forEach(ticket -> {
            assertNotNull(ticket.getTicketId(), "Ticket ID should not be null");
            assertNotNull(ticket.getVehicle(), "Vehicle should not be null");
            assertNotNull(ticket.getSpot(), "Spot should not be null");
            assertNotNull(ticket.getEntryTime(), "Entry time should not be null");
            assertTrue(ticket.getSpot().isOccupied(), "Spot should be marked as occupied");
            assertFalse(ticket.isProcessed(), "Ticket should not be processed yet");
            assertEquals(ticket.getVehicle(), ticket.getSpot().getParkedVehicle(),
                    "Spot should contain the correct vehicle");
        });

        // 7. System state should match reality
        Map<String, Object> status = (Map<String, Object>) parkingService.getParkingStatus();
        assertEquals(totalSpots, ((Number) status.get("totalSpots")).intValue(),
                "Total spots should be " + totalSpots);
        assertEquals(totalSpots, ((Number) status.get("occupiedSpots")).intValue(),
                "All spots should be occupied");
        assertEquals(0, ((Number) status.get("availableSpots")).intValue(),
                "No spots should be available");
        assertEquals(totalSpots, ((Number) status.get("activeTickets")).intValue(),
                "Active tickets should match occupied spots");

        // 8. Verify no vehicle was parked twice (data integrity)
        Set<String> parkedLicensePlates = successfulTickets.values().stream()
                .map(ticket -> ticket.getVehicle().getLicensePlate())
                .collect(Collectors.toSet());

        assertEquals(totalSpots, parkedLicensePlates.size(),
                "All parked vehicles should have unique license plates");

        System.out.println("✓ Test passed: " + successfulTickets.size() + " successful parks, "
                + failures.size() + " failures, 0 race conditions detected");
    }

    @Test
    @DisplayName("Test 2.2: Complex Concurrent Lifecycle - Parks, Exits, and Reservations")
    void testComplexConcurrentLifecycle() throws InterruptedException, ExecutionException {
        /*
         * This test validates:
         * 1. Mixed operations (park, exit, reserve) work correctly together
         * 2. Data consistency across multiple operation types
         * 3. No deadlocks occur in complex scenarios
         * 4. Proper cleanup and state transitions
         * 5. Reservations don't interfere with active parking
         * 6. System handles rapid state changes correctly
         */

        // Arrange - Create 20 spots
        int totalSpots = 20;
        for (int i = 1; i <= totalSpots; i++) {
            parkingService.addParkingSpot(new ParkingSpot("LIFECYCLE-" + i));
        }

        // Tracking structures
        ConcurrentHashMap<String, ParkingTicket> activeTickets = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, Reservation> activeReservations = new ConcurrentHashMap<>();
        AtomicInteger totalParks = new AtomicInteger(0);
        AtomicInteger totalExits = new AtomicInteger(0);
        AtomicInteger totalReservations = new AtomicInteger(0);
        List<Exception> unexpectedErrors = Collections.synchronizedList(new ArrayList<>());

        int totalOperations = 100;
        CountDownLatch doneLatch = new CountDownLatch(totalOperations);
        long startTime = System.currentTimeMillis();

        // Act - Submit 100 mixed operations
        Random random = new Random(12345); // Fixed seed for reproducibility

        for (int i = 0; i < totalOperations; i++) {
            final int operationId = i;

            executorService.submit(() -> {
                try {
                    int operationType = random.nextInt(100);

                    if (operationType < 40) {
                        // 40% - Park a vehicle
                        try {
                            Vehicle vehicle = new Vehicle("VEH-" + operationId, VehicleType.CAR);
                            ParkingTicket ticket = parkingService.parkVehicle(vehicle);
                            activeTickets.put(ticket.getTicketId(), ticket);
                            totalParks.incrementAndGet();

                            // Small delay to simulate real-world usage
                            Thread.sleep(random.nextInt(10));

                        } catch (IllegalStateException e) {
                            // Expected when lot is full
                            if (!e.getMessage().contains("No available parking spots") &&
                                    !e.getMessage().contains("already has an active parking ticket")) {
                                unexpectedErrors.add(e);
                            }
                        }

                    } else if (operationType < 70 && !activeTickets.isEmpty()) {
                        // 30% - Exit a vehicle (if any are parked)
                        try {
                            // Get a random active ticket
                            List<String> ticketIds = new ArrayList<>(activeTickets.keySet());
                            if (!ticketIds.isEmpty()) {
                                String ticketId = ticketIds.get(random.nextInt(ticketIds.size()));
                                ParkingTicket ticket = activeTickets.remove(ticketId);

                                if (ticket != null) {
                                    double fee = parkingService.exitVehicle(ticketId);
                                    assertTrue(fee > 0, "Fee should be positive");
                                    totalExits.incrementAndGet();
                                }
                            }
                        } catch (IllegalArgumentException | IllegalStateException e) {
                            // Expected if ticket already processed by another thread
                            if (!e.getMessage().contains("Invalid ticket ID") &&
                                    !e.getMessage().contains("already been processed")) {
                                unexpectedErrors.add(e);
                            }
                        }

                    } else {
                        // 30% - Create a reservation
                        try {
                            Vehicle vehicle = new Vehicle("RES-" + operationId, VehicleType.CAR);
                            LocalDateTime start = LocalDateTime.now().plusHours(2 + random.nextInt(10));
                            LocalDateTime end = start.plusHours(1 + random.nextInt(3));

                            Reservation reservation = parkingService.createReservation(
                                    "user-" + operationId,
                                    vehicle,
                                    start,
                                    end
                            );
                            activeReservations.put(reservation.getReservationId(), reservation);
                            totalReservations.incrementAndGet();

                        } catch (IllegalStateException e) {
                            // Expected when no spots available for reservation
                            if (!e.getMessage().contains("No spots available")) {
                                unexpectedErrors.add(e);
                            }
                        }
                    }

                } catch (Exception e) {
                    unexpectedErrors.add(e);
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        // Wait for all operations to complete
        boolean completed = doneLatch.await(60, TimeUnit.SECONDS);
        long duration = System.currentTimeMillis() - startTime;

        assertTrue(completed, "All operations should complete (no deadlock)");
        assertTrue(duration < 60000, "Should complete within 60 seconds");

        // Allow time for any final state updates
        Thread.sleep(100);

        // ============ CRITICAL ASSERTIONS ============

        // 1. No unexpected errors occurred
        if (!unexpectedErrors.isEmpty()) {
            System.err.println("Unexpected errors occurred:");
            unexpectedErrors.forEach(e -> e.printStackTrace());
        }
        assertEquals(0, unexpectedErrors.size(),
                "No unexpected errors should occur: " +
                        (unexpectedErrors.isEmpty() ? "" : unexpectedErrors.get(0).getMessage()));

        // 2. Operations completed successfully
        assertTrue(totalParks.get() > 0, "Some parking operations should succeed");
        assertTrue(totalExits.get() > 0, "Some exit operations should succeed");
        assertTrue(totalReservations.get() > 0, "Some reservations should succeed");

        System.out.println("Operations completed: Parks=" + totalParks.get() +
                ", Exits=" + totalExits.get() +
                ", Reservations=" + totalReservations.get());

        // 3. Exit count should not exceed park count
        assertTrue(totalExits.get() <= totalParks.get(),
                "Cannot exit more vehicles than were parked");

        // 4. System state consistency
        Map<String, Object> status = (Map<String, Object>) parkingService.getParkingStatus();
        long occupiedSpots = ((Number) status.get("occupiedSpots")).longValue();
        long availableSpots = ((Number) status.get("availableSpots")).longValue();
        int activeTicketCount = (int) status.get("activeTickets");

        // Total should equal original spot count
        assertEquals(totalSpots, occupiedSpots + availableSpots,
                "Total spots should remain constant");

        // Occupied spots should match active tickets
        assertEquals(occupiedSpots, activeTicketCount,
                "Occupied spots should match active ticket count");

        // Current state should match operations
        long expectedOccupied = totalParks.get() - totalExits.get();
        assertEquals(expectedOccupied, occupiedSpots,
                "Occupied spots should match (parks - exits)");

        // 5. Verify all remaining tickets are valid
        List<ParkingTicket> remainingTickets = parkingService.getActiveTickets();
        assertEquals(activeTicketCount, remainingTickets.size(),
                "Active ticket list should match count");

        remainingTickets.forEach(ticket -> {
            assertNotNull(ticket.getSpot(), "Ticket should have assigned spot");
            assertTrue(ticket.getSpot().isOccupied(), "Spot should be occupied");
            assertFalse(ticket.isProcessed(), "Active ticket should not be processed");
        });

        // 6. Verify all reservations are valid
        List<Reservation> allReservations = parkingService.getAllReservations();
        assertEquals(totalReservations.get(), allReservations.size(),
                "Reservation count should match");

        allReservations.forEach(reservation -> {
            assertNotNull(reservation.getReservationId(), "Reservation should have ID");
            assertTrue(reservation.getStatus() == ReservationStatus.CONFIRMED ||
                            reservation.getStatus() == ReservationStatus.ACTIVE,
                    "Reservation should be in valid state");
            assertTrue(reservation.getPaidAmount() > 0,
                    "Reservation should have positive fee");
        });

        // 7. Data integrity - No orphaned vehicles
        Set<String> ticketVehiclePlates = remainingTickets.stream()
                .map(t -> t.getVehicle().getLicensePlate())
                .collect(Collectors.toSet());

        assertEquals(remainingTickets.size(), ticketVehiclePlates.size(),
                "All active tickets should have unique vehicles");

        // 8. Performance characteristics
        double operationsPerSecond = (totalOperations * 1000.0) / duration;
        System.out.println("Performance: " + String.format("%.2f", operationsPerSecond) +
                " operations/second");
        assertTrue(operationsPerSecond > 10,
                "Should handle at least 10 operations per second");

        // 9. Final consistency check - manually verify spot occupancy
        long manualOccupiedCount = remainingTickets.stream()
                .filter(t -> t.getSpot().isOccupied())
                .count();

        assertEquals(occupiedSpots, manualOccupiedCount,
                "Spot occupancy should match ticket count");

        System.out.println("✓ Test passed: System maintained consistency through " +
                totalOperations + " concurrent mixed operations");
        System.out.println("  Final state: " + occupiedSpots + " occupied, " +
                availableSpots + " available, " +
                totalReservations.get() + " reservations");
    }
}
