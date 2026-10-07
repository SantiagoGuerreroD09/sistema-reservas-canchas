package com.santiago.reservations_service.models;

import com.santiago.reservations_service.entities.Reservation;

// La reserva junto con los datos de su horario traidos de courts-service.
public class ReservationDetail {

    private Reservation reservation;
    private Schedule schedule;

    public ReservationDetail(Reservation reservation, Schedule schedule) {
        this.reservation = reservation;
        this.schedule = schedule;
    }
    public Reservation getReservation() {
        return reservation;
    }
    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }
    public Schedule getSchedule() {
        return schedule;
    }
    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

}
