package com.tourguide;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Entry point - equivalent of the Node src/server.js + src/app.js pair. */
@SpringBootApplication
public class TourGuideApplication {
    public static void main(String[] args) {
        SpringApplication.run(TourGuideApplication.class, args);
    }
}
