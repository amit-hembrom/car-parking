package com.parking.model;

/**
 * Represents a parking spot with a unique identifier and occupancy status.
 * Each spot can hold one vehicle at a time.
 */
public class ParkingSpot {
    private final String spotId;
    private boolean occupied;
    private Vehicle parkedVehicle;

    /**
     * Creates a new ParkingSpot with the given ID.
     *
     * @param spotId unique identifier for the parking spot (e.g., "A1", "B2")
     * @throws IllegalArgumentException if spotId is null or empty
     */
    public ParkingSpot(String spotId) {
        if (spotId == null || spotId.trim().isEmpty()) {
            throw new IllegalArgumentException("Spot ID cannot be null or empty");
        }
        this.spotId = spotId;
        this.occupied = false;
        this.parkedVehicle = null;
    }

    public String getSpotId() {
        return spotId;
    }

    public boolean isOccupied() {
        return occupied;
    }

    public void setOccupied(boolean occupied) {
        this.occupied = occupied;
    }

    public Vehicle getParkedVehicle() {
        return parkedVehicle;
    }

    public void setParkedVehicle(Vehicle parkedVehicle) {
        this.parkedVehicle = parkedVehicle;
    }

    /**
     * Parks a vehicle in this spot.
     *
     * @param vehicle the vehicle to park
     * @throws IllegalStateException if the spot is already occupied
     */
    public void parkVehicle(Vehicle vehicle) {
        if (occupied) {
            throw new IllegalStateException("Spot is already occupied");
        }
        this.parkedVehicle = vehicle;
        this.occupied = true;
    }

    /**
     * Releases the spot, removing the parked vehicle.
     */
    public void releaseSpot() {
        this.parkedVehicle = null;
        this.occupied = false;
    }

    @Override
    public String toString() {
        return "ParkingSpot{" +
                "spotId='" + spotId + '\'' +
                ", occupied=" + occupied +
                ", parkedVehicle=" + parkedVehicle +
                '}';
    }
}