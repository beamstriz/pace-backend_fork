package com.agu.gestaoescalabackend;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GestaoEscalaBackendApplication implements CommandLineRunner {

	public static void main(String... args) {
		SpringApplication.run(GestaoEscalaBackendApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		System.out.println("la la la");
	}
}
