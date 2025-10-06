package com.dukhan.MQ.Helpers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class MqHelpersApplication {

	public static void main(String[] args) {
		SpringApplication.run(MqHelpersApplication.class, args);
	}

}
