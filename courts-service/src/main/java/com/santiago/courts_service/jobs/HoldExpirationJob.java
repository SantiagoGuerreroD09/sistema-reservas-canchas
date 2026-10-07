package com.santiago.courts_service.jobs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.santiago.courts_service.services.ScheduleService;

// Red de seguridad: libera los horarios cuyo hold vencio aunque reservations-service
// no haya avisado (caido, timeout, etc.). Asi un horario nunca queda bloqueado para siempre.
@Component
public class HoldExpirationJob {

    private static final Logger log = LoggerFactory.getLogger(HoldExpirationJob.class);

    final private ScheduleService service;

    public HoldExpirationJob(ScheduleService service) {
        this.service = service;
    }

    @Scheduled(fixedDelayString = "${config.hold.release-interval}")
    public void releaseExpiredHolds() {
        int released = service.releaseExpiredHolds();
        if (released > 0) {
            log.info("Horarios liberados por hold vencido: {}", released);
        }
    }
}
