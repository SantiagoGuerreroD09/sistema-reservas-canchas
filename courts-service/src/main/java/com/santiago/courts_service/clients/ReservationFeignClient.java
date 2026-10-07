package com.santiago.courts_service.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.santiago.courts_service.models.Reservation;

//@FeignClient(url = "localhost:8002")
@FeignClient(name = "reservations-service", path = "/reservations")
public interface ReservationFeignClient {

    @GetMapping("/{id}")
    Reservation details(@PathVariable Long id);

}
