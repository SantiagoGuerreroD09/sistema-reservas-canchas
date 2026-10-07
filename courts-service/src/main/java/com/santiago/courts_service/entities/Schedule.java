package com.santiago.courts_service.entities;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "schedules")
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "court_id")
    private Court court;

    private LocalDate date;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    private ScheduleStatus status;

    // Hasta cuándo dura el bloqueo temporal (hold) mientras se paga. Null si no está en HELD.
    @Column(name = "held_until")
    private LocalDateTime heldUntil;

    // Puerto de la instancia que respondio; no se guarda en la base. Sirve para
    // verificar el balanceo de carga cuando hay varias instancias de courts-service.
    @Transient
    private int port;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Court getCourt() {
        return court;
    }

    public void setCourt(Court court) {
        this.court = court;
    }

    public Long getCourtId() {
        return court != null ? court.getId() : null;
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

    public ScheduleStatus getStatus() {
        return status;
    }

    public void setStatus(ScheduleStatus status) {
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

}
