import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.db.tradestore.application.TradeStoreApp;
import com.db.tradestore.service.AppConfig;
import com.db.tradestore.service.TradeStoreService;
import com.db.tradestore.vo.TradeVO;

@SpringBootTest(classes = TradeStoreApp.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class TradeStoreServiceTests {

	@Autowired
	private TradeStoreService tradeService;

	@Autowired
	private AppConfig appConfig;

	private Map<String, TradeVO> tradeMap = new HashMap<String, TradeVO>();

	@Test
	public void whenlowerVersion_thenTradeRejected() {
		try {
			if (tradeMap.isEmpty()) {
				tradeMap = tradeService.loadTradeStoreData(appConfig.getTradeStoreFile());
			}
			String tradeLowerVersionStr = "T2|1|CP-2|B1|11/11/2020";
			TradeVO tradeVO = tradeService.processTradeObj(tradeLowerVersionStr);
			assertNull(tradeVO);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void whenlowerMaturityDate_thenTradeRejected() {
		try {
			if (tradeMap.isEmpty()) {
				tradeMap = tradeService.loadTradeStoreData(appConfig.getTradeStoreFile());
			}
			String tradeLowerMaturityDateStr = "T1|1|CP-2|B1|11/11/2020";
			TradeVO tradeVO = tradeService.processTradeObj(tradeLowerMaturityDateStr);
			assertNull(tradeVO);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	@Test
	public void whenMaturityDatePassed_thenTradeAutoExpired() {
		try {
			if (tradeMap.isEmpty()) {
				tradeMap = tradeService.loadTradeStoreData(appConfig.getTradeStoreFile());
			}
			TradeVO expiredTradeVO = tradeMap.get("T2");
			assertEquals("Y", expiredTradeVO.getExpired());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	@Test
	public void whenAllValidationPassed_thenTradeAccepted() {
		try {
			tradeService.processTrade();
			if (tradeMap.isEmpty()) {
				tradeMap = tradeService.loadTradeStoreData(appConfig.getTradeStoreFile());
			}
			assertEquals(2, tradeMap.size());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
