package com.chatapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Real-Time Chat Application.
 *
 * <p>Spring Boot auto-configures embedded Tomcat, JPA, H2 console,
 * WebSocket broker, and static resource serving from /static.</p>
 */
@SpringBootApplication
public class ChatApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChatApplication.class, args);
    }
}
