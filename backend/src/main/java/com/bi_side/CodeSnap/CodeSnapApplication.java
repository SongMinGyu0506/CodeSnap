package com.bi_side.CodeSnap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@EnableAspectJAutoProxy
@SpringBootApplication
public class CodeSnapApplication {

	public static void main(String[] args) {
		//System.setProperty("spring.devtools.restart.enabled","false");
		SpringApplication.run(CodeSnapApplication.class, args);
	}

}
