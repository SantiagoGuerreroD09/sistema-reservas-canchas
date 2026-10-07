package com.santiago.reservations_service.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

import com.santiago.reservations_service.models.Schedule;

@FeignClient(name = "courts-service", path = "/schedules")
public interface CourtFeignClient {

    @GetMapping("/{id}")
    Schedule scheduleDetails(@PathVariable Long id);

    @PutMapping("/{id}/hold")
    Schedule hold(@PathVariable Long id);

    @PutMapping("/{id}/reserve")
    Schedule reserve(@PathVariable Long id);

    @PutMapping("/{id}/release")
    Schedule release(@PathVariable Long id);

}
