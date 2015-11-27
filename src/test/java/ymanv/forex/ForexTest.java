/**
 * Copyright (C) 2015 https://github.com/ymanv
 *
 * This software is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package ymanv.forex;

import static ymanv.forex.Utils.rate;
import static ymanv.forex.util.CurrencyUtils.EUR;
import static ymanv.forex.util.CurrencyUtils.USD;
import static ymanv.forex.util.DateUtils.DATE_TIME_WITH_TZ;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.common.eventbus.EventBus;

import ymanv.forex.Forex;
import ymanv.forex.ForexApplication;
import ymanv.forex.http.URLConnectionHandler;
import ymanv.forex.model.entity.rate.RateEntity;
import ymanv.forex.model.repositories.HistoricalRateRepository;
import ymanv.forex.model.repositories.LatestRateRepository;
import ymanv.forex.provider.impl.Yahoo;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = ForexApplication.class)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class ForexTest {

	private Forex forex;

	@Autowired
	private LatestRateRepository latestRepo;

	@Autowired
	private HistoricalRateRepository repo;

	@Mock
	private URLConnectionHandler handler;

	@InjectMocks
	private Yahoo defaultProvider;

	@Mock
	private EventBus bus;

	private static String RESULT_20141219;

	private static String RESULT_20150920;

	@BeforeClass
	public static void setUpBeforeClass() throws IOException, URISyntaxException {
		RESULT_20141219 = Utils.readFile("/provider/yahoo_commodities/quote_20141219.json");
		RESULT_20150920 = Utils.readFile("/provider/yahoo_commodities/quote_20150920.json");
	}

	@Before
	public void initMocks() throws IOException {
		MockitoAnnotations.initMocks(this);
		when(handler.sendGet(anyString())).thenReturn(RESULT_20141219);
		forex = new Forex(repo, latestRepo, defaultProvider, bus);
	}

	@Test
	public void testUpdateRates() throws IOException, Exception {
		// when
		forex.updateRates();

		// then
		verify(bus).post(any());

		List<? extends RateEntity> hRates = repo.findAll();
		List<? extends RateEntity> lRates = latestRepo.findAll();

		assertThat(hRates).hasSize(172).containsOnlyOnce( //
				rate(USD, EUR, new BigDecimal("0.817595"), DATE_TIME_WITH_TZ.parse("2014-12-19 21:40:32.0 GMT")));

		assertThat(lRates).hasSize(172).containsOnlyOnce( //
				rate(USD, EUR, new BigDecimal("0.817595"), DATE_TIME_WITH_TZ.parse("2014-12-19 21:40:32.0 GMT")));
	}

	@Test
	public void testUpdateRates_TwoCallsWithSameData() throws IOException, Exception {
		// when
		forex.updateRates();
		forex.updateRates();

		// then
		verify(bus).post(any());

		List<? extends RateEntity> hRates = repo.findAll();
		List<? extends RateEntity> lRates = latestRepo.findAll();

		assertThat(hRates).hasSize(172).containsOnlyOnce( //
				rate(USD, EUR, new BigDecimal("0.817595"), DATE_TIME_WITH_TZ.parse("2014-12-19 21:40:32.0 GMT")));

		assertThat(lRates).hasSize(172).containsOnlyOnce( //
				rate(USD, EUR, new BigDecimal("0.817595"), DATE_TIME_WITH_TZ.parse("2014-12-19 21:40:32.0 GMT")));
	}

	@Test
	public void testUpdateRates_TwoCallsWithNotSameData() throws IOException, Exception {
		when(handler.sendGet(anyString())).thenReturn(RESULT_20141219, RESULT_20150920);

		// when
		forex.updateRates();
		forex.updateRates();

		// then
		verify(bus, times(2)).post(any());

		List<? extends RateEntity> hRates = repo.findAll();
		List<? extends RateEntity> lRates = latestRepo.findAll();

		assertThat(hRates).hasSize(344).containsOnlyOnce( //
				rate(USD, EUR, new BigDecimal("0.817595"), DATE_TIME_WITH_TZ.parse("2014-12-19 21:40:32.0 GMT")), //
				rate(USD, EUR, new BigDecimal("0.884291"), DATE_TIME_WITH_TZ.parse("2015-09-20 18:19:11.0 GMT")));

		assertThat(lRates).hasSize(172).doesNotContain( //
				rate(USD, EUR, new BigDecimal("0.817595"), DATE_TIME_WITH_TZ.parse("2014-12-19 21:40:32.0 GMT")));
		assertThat(lRates).containsOnlyOnce( //
				rate(USD, EUR, new BigDecimal("0.884291"), DATE_TIME_WITH_TZ.parse("2015-09-20 18:19:11.0 GMT")));

	}
}
