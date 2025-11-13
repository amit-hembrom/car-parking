package com.parking.model;

import java.util.Objects;

/**
 * Represents a vehicle with a license plate and type.
 * This is the main entity for vehicles in the parking system.
 */
public class Vehicle {
    private final String licensePlate;
    private final VehicleType type;

    /**
     * Creates a new Vehicle instance.
     *
     * @param licensePlate the vehicle's license plate (cannot be null or empty)
     * @param type the type of vehicle (MOTORCYCLE, CAR, VAN, or BUS)
     * @throws IllegalArgumentException if licensePlate is null/empty or type is null
     */
    public Vehicle(String licensePlate, VehicleType type) {
        if (licensePlate == null || licensePlate.trim().isEmpty()) {
            throw new IllegalArgumentException("License plate cannot be null or empty");
        }
        if (type == null) {
            throw new IllegalArgumentException("Vehicle type cannot be null");
        }
        this.licensePlate = licensePlate;
        this.type = type;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public VehicleType getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vehicle vehicle = (Vehicle) o;
        return Objects.equals(licensePlate, vehicle.licensePlate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(licensePlate);
    }

    @Override
    public String toString() {
        return "Vehicle{" +
                "licensePlate='" + licensePlate + '\'' +
                ", type=" + type +
                '}';
    }
}