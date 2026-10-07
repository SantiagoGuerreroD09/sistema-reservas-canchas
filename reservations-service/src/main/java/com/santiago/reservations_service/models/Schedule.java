package com.santiago.reservations_service.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

// Copia del horario que expone courts-service.
public class Schedule {

    private Long id;
    private Long courtId;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private BigDecimal price;
    private String status;
    private LocalDateTime heldUntil;
    // Puerto de la instancia de courts-service que respondio (verificacion del balanceo).
    private int port;

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Long getCourtId() {
        return courtId;
    }
    public void setCourtId(Long courtId) {
        this.courtId = courtId;
    }
    public LocalDate getDate() {
        return date;
    }
    public void setDate(LocalDate date) {
        this.date = date;
    }
    public LocalTime getStartTime() {
        return startTime;
    }
    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }
    public LocalTime getEndTime() {
        return endTime;
    }
    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }
    public BigDecimal getPrice() {
        return price;
    }
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public LocalDateTime getHeldUntil() {
        return heldUntil;
    }
    public void setHeldUntil(LocalDateTime heldUntil) {
        this.heldUntil = heldUntil;
    }
    public int getPort() {
        return port;
    }
    public void setPort(int port) {
        this.port = port;
    }

    public LocalDateTime startDateTime() {
        return LocalDateTime.of(date, startTime);
    }

}
