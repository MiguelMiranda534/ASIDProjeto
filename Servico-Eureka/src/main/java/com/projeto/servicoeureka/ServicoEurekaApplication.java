package com.projeto.servicoeureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class ServicoEurekaApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServicoEurekaApplication.class, args);
	}
}
