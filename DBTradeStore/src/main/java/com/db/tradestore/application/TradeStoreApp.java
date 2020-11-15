package com.db.tradestore.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.db.tradestore.service.TradeStoreService;

@SpringBootApplication(scanBasePackages = { "com.db.tradestore" })
public class TradeStoreApp implements CommandLineRunner {

	private static final Logger logger = LoggerFactory.getLogger(TradeStoreApp.class);

	@Autowired
	TradeStoreService tradeService;

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(TradeStoreApp.class);
		app.run(args);
	}

	public void run(String... strings) throws Exception {
		logger.info("Trade Processing Started");
		tradeService.processTrade();
		logger.info("Trade Processing End");
	}
}
