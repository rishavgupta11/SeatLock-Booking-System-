package com.seatlock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // Background tasks run karne ke liye enable kiya
public class SeatLockEngineApplication {

	public static void main(String[] args) {
		SpringApplication.run(SeatLockEngineApplication.class, args);
	}
}
