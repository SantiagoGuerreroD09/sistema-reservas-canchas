package com.santiago.courts_service;

import java.time.Clock;
import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Value("${config.time-zone}")
    private String timeZone;

    // Los horarios se guardan en hora local de las canchas; todas las comparaciones
    // con "ahora" usan este reloj para no depender de la zona del servidor/contenedor.
    @Bean
    Clock clock() {
        return Clock.system(ZoneId.of(timeZone));
    }
}
