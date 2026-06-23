package com.example.attendee_university;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.web.bind.annotation.CrossOrigin;

@SpringBootApplication
@EnableCaching
@CrossOrigin(origins = "http://localhost:3000")
@OpenAPIDefinition(
        info =
        @Info(
                title = "HRD EventHub API V5",
                version = "V5",
                description =
                        """
                                HRD EventHub is a backend service for managing alumni, students,
                                and organizational events across generations and workspaces.
                                """),
        servers = {
                @Server(url = "http://localhost:9090", description = "Local Development Server"),
                @Server(url = "http://localhost:8080", description = "Local Development Server"),
                @Server(url = "https://api.hrdevent.app/", description = "Production Server"),
                @Server(url = "https://api.hrdevent.app/", description = "Production for dev Server"),
        })
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER)
public class AttendeeUniversityApplication {

    public static void main(String[] args) {
        SpringApplication.run(AttendeeUniversityApplication.class, args);
    }

}
