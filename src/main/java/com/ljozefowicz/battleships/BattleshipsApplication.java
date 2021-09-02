package com.ljozefowicz.battleships;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@SpringBootApplication
@ServletComponentScan
public class BattleshipsApplication {

	public static void main(String[] args) {
		SpringApplication.run(BattleshipsApplication.class, args);
	}

}
