package com.serviceauth.serviicocatalogo;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableEurekaClient
@SpringBootApplication
public class ServiicoCatalogoApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServiicoCatalogoApplication.class, args);
	}

}
