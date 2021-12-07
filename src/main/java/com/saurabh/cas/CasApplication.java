package com.saurabh.cas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CasApplication {

	public static void main(String[] args) {
		SpringApplication.run(CasApplication.class, args);
	}

	// docker run -p9042:9042 --name cassandra -d cassandra:latest

}