package com.santiago.reservations_service.jobs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.santiago.reservations_service.services.ReservationService;

// Marca como EXPIRED las reservas cuyo tiempo para pagar se vencio.
@Component
public class HoldExpirationJob {

    private static final Logger log = LoggerFactory.getLogger(HoldExpirationJob.class);

    final private ReservationService service;

    public HoldExpirationJob(ReservationService service) {
        this.service = service;
    }

    @Scheduled(fixedDelayString = "${config.hold.expiration-interval}")
    public void expirePendingHolds() {
        int expired = service.expirePendingHolds();
        if (expired > 0) {
            log.info("Reservas expiradas por falta de pago: {}", expired);
        }
    }
}
