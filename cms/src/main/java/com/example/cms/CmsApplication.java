package com.example.cms;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class CmsApplication {

	public static void main(String[] args) {
		SpringApplication.run(CmsApplication.class, args);
	}


//	@Bean
//	CommandLineRunner runner(UserRepositroy repository){
//		return  args -> {
//            User user = new User(
//					"Akshay",
//					"Bidwai",
//					"akshay@gmail.com",
//					"01-01-1997",
//					"pass123"
//			);
//			repository.insert(user);
//		};
//	}
}
