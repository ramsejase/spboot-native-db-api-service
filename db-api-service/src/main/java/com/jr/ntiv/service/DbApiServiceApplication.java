package com.rj.ntiv.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan("com.rj.ntiv")
@SpringBootApplication(proxyBeanMethods = false)
public class DbApiServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(DbApiServiceApplication.class, args);
	}

}
