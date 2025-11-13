package com.parking.service;

import com.parking.model.VehicleType;

import java.time.Duration;
import java.time.LocalDateTime;

public class PricingService {
    private static final double MOTORCYCLE_RATE = 2.0;
    private static final double CAR_RATE = 5.0;
    private static final double VAN_RATE = 7.5;
    private static final double BUS_RATE = 10.0;
    private static final double RESERVATION_PREMIUM = 1.2;

    public double calculateFee(VehicleType vehicleType, LocalDateTime entryTime, LocalDateTime exitTime) {
        if (entryTime == null || exitTime == null) {
            throw new IllegalArgumentException("Entry and exit times cannot be null");
        }
        if (exitTime.isBefore(entryTime)) {
            throw new IllegalArgumentException("Exit time cannot be before entry time");
        }

        Duration duration = Duration.between(entryTime, exitTime);
        double hours = Math.max(1, Math.ceil(duration.toMinutes() / 60.0));

        double hourlyRate = getHourlyRate(vehicleType);
        return hourlyRate * hours;
    }

    public double calculateReservationFee(VehicleType vehicleType, LocalDateTime startTime, LocalDateTime endTime) {
        double baseFee = calculateFee(vehicleType, startTime, endTime);
        return baseFee * RESERVATION_PREMIUM;
    }

    private double getHourlyRate(VehicleType vehicleType) {
        return switch (vehicleType) {
            case MOTORCYCLE -> MOTORCYCLE_RATE;
            case CAR -> CAR_RATE;
            case VAN -> VAN_RATE;
            case BUS -> BUS_RATE;
        };
    }

    public double getReservationPremium() {
        return RESERVATION_PREMIUM;
    }
}