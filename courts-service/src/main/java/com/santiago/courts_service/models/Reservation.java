package com.santiago.courts_service.models;

// Copia de la reserva que expone reservations-service (solo los campos que se usan aqui).
public class Reservation {

    private Long id;
    private Long userId;
    private Long scheduleId;
    private String status;

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Long getUserId() {
        return userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    public Long getScheduleId() {
        return scheduleId;
    }
    public void setScheduleId(Long scheduleId) {
        this.scheduleId = scheduleId;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

}
