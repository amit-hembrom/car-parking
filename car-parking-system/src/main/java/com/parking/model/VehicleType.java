package com.parking.model;

/**
 * Enum representing different types of vehicles that can be parked.
 * Each type has an associated size that determines parking space requirements.
 */
public enum VehicleType {
    MOTORCYCLE(1),
    CAR(2),
    VAN(3),
    BUS(4);

    private final int size;

    VehicleType(int size) {
        this.size = size;
    }

    public int getSize() {
        return size;
    }
}