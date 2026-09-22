package com.trackerapp.tracker_app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TrackerAppApplication {

	public static void main(String[] args) {

        SpringApplication.run(TrackerAppApplication.class, args);
	}

}
