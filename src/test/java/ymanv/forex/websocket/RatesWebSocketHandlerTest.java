package ymanv.forex.websocket;

import static org.assertj.core.api.Assertions.assertThat;
import static ymanv.forex.provider.impl.OfdpCrudOil.BRE;
import static ymanv.forex.util.CurrencyUtils.USD;
import static ymanv.forex.util.DateUtils.DATE_TIME_WITH_TZ;

import java.math.BigDecimal;

import org.assertj.core.util.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import ymanv.forex.ForexApplication;
import ymanv.forex.Utils;
import ymanv.forex.model.entity.rate.RateEntity;
import ymanv.forex.websocket.RatesWebSocketHandler;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = ForexApplication.class)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class RatesWebSocketHandlerTest {

	@Autowired
	private RatesWebSocketHandler handler;

	@Test
	public void testSerializeRates() throws Exception {
		// given
		RateEntity rate = Utils.rate(BRE, USD, new BigDecimal("57.8"), DATE_TIME_WITH_TZ.parse("2015-04-07 02:00:00.0 CEST"));

		// when
		String result = handler.serializeRates(Lists.newArrayList(rate));

		// then
		assertThat(result).isEqualTo("[{\"fromcur\":\"BRE\",\"tocur\":\"USD\",\"value\":57.8,\"date\":1428364800000}]");
	}
}
