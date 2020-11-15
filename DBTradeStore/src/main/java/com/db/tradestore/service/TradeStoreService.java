package com.db.tradestore.service;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.db.tradestore.application.TradeStoreApp;
import com.db.tradestore.exception.TradeRejectException;
import com.db.tradestore.vo.TradeVO;

@Component
public class TradeStoreService {

	private static final Logger logger = LoggerFactory.getLogger(TradeStoreApp.class);

	private static final String headerStr = "Trade Id|Version|Counter-Party Id|Book-Id|Maturity Date|Created Date|Expired";
	@Autowired
	private AppConfig appConfig;

	private Map<String, TradeVO> tradeMap = new HashMap<String, TradeVO>();

	public void processTrade() {
		logger.info("Inside TradeStoreService::processTrade");
		try {
			tradeMap = loadTradeStoreData(appConfig.getTradeStoreFile());
			List<TradeVO> tradeList = new ArrayList<TradeVO>();
			Stream<String> tradeStream = readFile(appConfig.getIncomingTradeFile());
			tradeList = tradeStream.skip(1).map(trade -> processTradeObj(trade)).filter(Objects::nonNull)
					.collect(Collectors.toList());
			logger.info("Processed Trade List -->" + tradeList);
			if (!tradeMap.isEmpty()) {
				writeToFile(new ArrayList<TradeVO>(tradeMap.values()));
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			logger.error("Exception inside TradeStoreService::" + e.getMessage());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error("Exception inside TradeStoreService::" + e.getMessage());
		}
	}

	private Stream<String> readFile(String path) throws IOException {
		logger.info("Inside TradeStoreService::readFile");
		BufferedReader bufferReader = Files.newBufferedReader(Paths.get(path));
		Stream<String> tradeStream = bufferReader.lines();
		return tradeStream;
	}

	public Map<String, TradeVO> loadTradeStoreData(String path) throws IOException {
		logger.info("Inside TradeStoreService::loadTradeStoreData");
		Stream<String> tradeStream = readFile(path);
		Map<String, TradeVO> tradeMap = tradeStream.skip(1).map(trade -> populateTradeObj(trade))
				.filter(Objects::nonNull).collect(Collectors.toMap(TradeVO::getTradeId, trade -> trade));
		return tradeMap;

	}

	private void writeToFile(List<TradeVO> tradeList) throws IOException {
		logger.info("Inside TradeStoreService::writeToFile");
		List<String> tradeStrList = new ArrayList<String>();
		tradeStrList.add(headerStr);
		tradeStrList.addAll(tradeList.stream().map(tradeVO -> writeTradeObj(tradeVO)).collect(Collectors.toList()));
		Files.write(Paths.get(appConfig.getTradeStoreFile()), tradeStrList, StandardOpenOption.CREATE);
	}

	private String writeTradeObj(TradeVO tradeVO) {
		logger.info("Inside TradeStoreService::writeToFile");
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
		StringBuffer tradeStr = new StringBuffer();
		tradeStr.append(tradeVO.getTradeId()).append("|").append(tradeVO.getVersion()).append("|")
				.append(tradeVO.getCounterPartyId()).append("|").append(tradeVO.getBookId()).append("|")
				.append(formatter.format(tradeVO.getMaturityDate())).append("|")
				.append(formatter.format(tradeVO.getCreatedDate())).append("|").append(tradeVO.getExpired())
				.append("\n");
		return tradeStr.toString();
	}

	private TradeVO populateTradeObj(String tradeStr) {
		logger.info("Inside TradeStoreService::populateTradeObj");
		TradeVO tradeVO = null;
		if (!tradeStr.isEmpty()) {
			tradeVO = new TradeVO();
			String[] tradeStrArr = tradeStr.split("\\|");
			SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
			if (tradeStrArr.length > 0) {
				tradeVO.setTradeId(tradeStrArr[0]);
				tradeVO.setVersion(Integer.parseInt(tradeStrArr[1]));
				tradeVO.setCounterPartyId(tradeStrArr[2]);
				tradeVO.setBookId(tradeStrArr[3]);
				try {
					if (null != tradeStrArr[4]) {
						tradeVO.setMaturityDate(formatter.parse(tradeStrArr[4]));
					}
					if (tradeStrArr.length > 5 && null != tradeStrArr[5]) {
						tradeVO.setCreatedDate(formatter.parse(tradeStrArr[5]));
					}
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					logger.error("Exception inside populateTradeObj::" + e.getMessage());
				}
				if (tradeStrArr.length > 6 && null != tradeStrArr[6]) {
					if (tradeVO.getMaturityDate().before(new Date())) {
						tradeVO.setExpired("Y");
					} else {
						tradeVO.setExpired(tradeStrArr[6]);
					}
				}
			}
		}
		return tradeVO;
	}

	public TradeVO processTradeObj(String tradeStr) {
		logger.info("Inside TradeStoreService::processTradeObj");
		TradeVO tradeVO = new TradeVO();
		Date currentDate = new Date();
		tradeVO = populateTradeObj(tradeStr);
		try {
			if (!tradeMap.isEmpty() && tradeMap.containsKey(tradeVO.getTradeId())) {
				TradeVO existingTradeVO = tradeMap.get(tradeVO.getTradeId());
				if (tradeVO.getVersion().intValue() < existingTradeVO.getVersion().intValue()) {
					throw new TradeRejectException(
							"Trade::" + tradeVO.getTradeId() + " got rejected due to lower version.");
				}

			}
			if (tradeVO.getMaturityDate().before(currentDate)) {
				throw new TradeRejectException("Trade::" + tradeVO.getTradeId()
						+ " got rejected due to maturity date less than today's date.");
			}
			tradeVO.setCreatedDate(currentDate);
			tradeVO.setExpired("N");
			tradeMap.put(tradeVO.getTradeId(), tradeVO);
		} catch (TradeRejectException exception) {
			logger.error("TradeRejectException ::" + exception.getMessage());
			tradeVO = null;
		}
		return tradeVO;
	}
}
