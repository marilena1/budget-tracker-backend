package com.budgettracker.budget_tracker_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@SpringBootApplication
@EnableMongoAuditing
public class BudgetTrackerBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BudgetTrackerBackendApplication.class, args);
	}

}
