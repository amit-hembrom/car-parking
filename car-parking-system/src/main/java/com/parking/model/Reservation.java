package com.parking.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Reservation {
    private final String reservationId;
    private final String userId;
    private final Vehicle vehicle;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private ParkingSpot assignedSpot;
    private ReservationStatus status;
    private double paidAmount;

    public Reservation(String reservationId, String userId, Vehicle vehicle,
                       LocalDateTime startTime, LocalDateTime endTime) {
        if (reservationId == null || reservationId.trim().isEmpty()) {
            throw new IllegalArgumentException("Reservation ID cannot be null or empty");
        }
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }
        if (vehicle == null) {
            throw new IllegalArgumentException("Vehicle cannot be null");
        }
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("Start and end times cannot be null");
        }
        if (startTime.isAfter(endTime) || startTime.isEqual(endTime)) {
            throw new IllegalArgumentException("Start time must be before end time");
        }
        this.reservationId = reservationId;
        this.userId = userId;
        this.vehicle = vehicle;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = ReservationStatus.PENDING;
    }

    public String getReservationId() {
        return reservationId;
    }

    public String getUserId() {
        return userId;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public ParkingSpot getAssignedSpot() {
        return assignedSpot;
    }

    public void setAssignedSpot(ParkingSpot assignedSpot) {
        this.assignedSpot = assignedSpot;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    public double getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(double paidAmount) {
        this.paidAmount = paidAmount;
    }

    public boolean overlapsWith(Reservation other) {
        return !(this.endTime.isBefore(other.startTime) || this.endTime.isEqual(other.startTime) ||
                this.startTime.isAfter(other.endTime) || this.startTime.isEqual(other.endTime));
    }

    public boolean isActive(LocalDateTime currentTime) {
        return status == ReservationStatus.CONFIRMED &&
                !currentTime.isBefore(startTime) &&
                currentTime.isBefore(endTime);
    }

    public boolean isExpired(LocalDateTime currentTime) {
        return currentTime.isAfter(endTime);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reservation that = (Reservation) o;
        return Objects.equals(reservationId, that.reservationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reservationId);
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "reservationId='" + reservationId + '\'' +
                ", userId='" + userId + '\'' +
                ", vehicle=" + vehicle +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", assignedSpot=" + assignedSpot +
                ", status=" + status +
                ", paidAmount=" + paidAmount +
                '}';
    }
}