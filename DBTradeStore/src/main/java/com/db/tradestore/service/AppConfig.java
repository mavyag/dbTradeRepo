package com.db.tradestore.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value = { "classpath:application.properties" })
public class AppConfig {

	@Value("${incoming.trade.file}")
	private String incomingTradeFile;

	@Value("${trade.store.file}")
	private String tradeStoreFile;

	public String getIncomingTradeFile() {
		return incomingTradeFile;
	}

	public void setIncomingTradeFile(String incomingTradeFile) {
		this.incomingTradeFile = incomingTradeFile;
	}

	public String getTradeStoreFile() {
		return tradeStoreFile;
	}

	public void setTradeStoreFile(String tradeStoreFile) {
		this.tradeStoreFile = tradeStoreFile;
	}

}
