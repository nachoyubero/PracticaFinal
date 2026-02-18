package edu.comillas.icai.gitt.pat.spring.padelapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PadelappApplication {

	public static void main(String[] args) {
		SpringApplication.run(PadelappApplication.class, args);
	}

}
