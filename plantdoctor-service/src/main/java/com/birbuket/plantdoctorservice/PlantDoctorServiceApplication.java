package com.birbuket.plantdoctorservice;

import com.birbuket.common.exception.GlobalExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@Import(GlobalExceptionHandler.class)
public class PlantDoctorServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(PlantDoctorServiceApplication.class, args);
    }
}
