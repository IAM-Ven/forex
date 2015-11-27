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
import static ymanv.forex.provider.impl.OfdpCrudOil.BRE;
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
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.common.eventbus.EventBus;

import ymanv.forex.ForexApplication;
import ymanv.forex.Oil;
import ymanv.forex.http.URLConnectionHandler;
import ymanv.forex.model.entity.rate.RateEntity;
import ymanv.forex.model.repositories.HistoricalRateRepository;
import ymanv.forex.model.repositories.LatestRateRepository;
import ymanv.forex.provider.impl.OfdpCrudOil;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = ForexApplication.class)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@Sql("/sql/insert_data.sql")
public class OilTest {

	private Oil oil;

	@Autowired
	private LatestRateRepository latestRepo;

	@Autowired
	private HistoricalRateRepository repo;

	@Mock
	private URLConnectionHandler handler;

	@InjectMocks
	private OfdpCrudOil defaultProvider;

	@Mock
	private EventBus bus;

	private static String BRENT_LIGHT, BRENT_LIGHT_20150918;

	@BeforeClass
	public static void setUpBeforeClass() throws IOException, URISyntaxException {
		BRENT_LIGHT = Utils.readFile("/provider/ofdp/brent-light.json");
		BRENT_LIGHT_20150918 = Utils.readFile("/provider/ofdp/brent-light_20150918.json");
	}

	@Before
	public void initMocks() throws IOException {
		MockitoAnnotations.initMocks(this);
		when(handler.sendGet(anyString())).thenReturn(BRENT_LIGHT);
		oil = new Oil(repo, latestRepo, defaultProvider, bus);
	}

	@Test
	public void testUpdateRates() throws Exception {
		// given
		RateEntity expectedOldLatest = rate(BRE, USD, new BigDecimal("55.18"), DATE_TIME_WITH_TZ.parse("2015-04-06 02:00:00.0 CEST"));
		RateEntity expectedAdded = rate(BRE, USD, new BigDecimal("57.8"), DATE_TIME_WITH_TZ.parse("2015-04-07 02:00:00.0 CEST"));
		RateEntity expectedNewLatest = rate(BRE, USD, new BigDecimal("58.3"), DATE_TIME_WITH_TZ.parse("2015-04-08 02:00:00.0 CEST"));

		// when
		oil.updateRates();

		// then
		List<? extends RateEntity> hRates = repo.findAll();
		List<? extends RateEntity> lRates = latestRepo.findAll();

		assertThat(hRates).hasSize(14);
		assertThat(hRates).containsOnlyOnce(expectedOldLatest, expectedAdded, expectedNewLatest);

		assertThat(lRates).hasSize(2);
		assertThat(lRates).doesNotContain(expectedOldLatest);
		assertThat(lRates).containsOnlyOnce(expectedNewLatest);

		verify(bus).post(any());
	}

	@Test
	public void testUpdateRates_TwoCallsWithSameData() throws Exception {
		// given
		RateEntity expectedOldLatest = rate(BRE, USD, new BigDecimal("55.18"), DATE_TIME_WITH_TZ.parse("2015-04-06 02:00:00.0 CEST"));
		RateEntity expectedAdded = rate(BRE, USD, new BigDecimal("57.8"), DATE_TIME_WITH_TZ.parse("2015-04-07 02:00:00.0 CEST"));
		RateEntity expectedNewLatest = rate(BRE, USD, new BigDecimal("58.3"), DATE_TIME_WITH_TZ.parse("2015-04-08 02:00:00.0 CEST"));

		// when
		oil.updateRates();

		// then
		List<? extends RateEntity> hRates = repo.findAll();
		List<? extends RateEntity> lRates = latestRepo.findAll();

		assertThat(hRates).hasSize(14);
		assertThat(hRates).containsOnlyOnce(expectedOldLatest, expectedAdded, expectedNewLatest);

		assertThat(lRates).hasSize(2);
		assertThat(lRates).doesNotContain(expectedOldLatest);
		assertThat(lRates).containsOnlyOnce(expectedNewLatest);

		verify(bus).post(any());
	}

	@Test
	public void testUpdateRates_TwoCallsWithNotSameData() throws Exception {
		when(handler.sendGet(anyString())).thenReturn(BRENT_LIGHT, BRENT_LIGHT_20150918);

		// given
		RateEntity expectedOldLatest = rate(BRE, USD, new BigDecimal("55.18"), DATE_TIME_WITH_TZ.parse("2015-04-06 02:00:00.0 CEST"));
		RateEntity expectedAdded = rate(BRE, USD, new BigDecimal("57.8"), DATE_TIME_WITH_TZ.parse("2015-04-07 02:00:00.0 CEST"));
		RateEntity expectedFirstCallLatest = rate(BRE, USD, new BigDecimal("58.3"), DATE_TIME_WITH_TZ.parse("2015-04-08 02:00:00.0 CEST"));
		RateEntity expectedNewLatest = rate(BRE, USD, new BigDecimal("49.26"), DATE_TIME_WITH_TZ.parse("2015-09-18 02:00:00.0 CEST"));

		// when
		oil.updateRates();
		oil.updateRates();

		// then
		List<? extends RateEntity> hRates = repo.findAll();
		List<? extends RateEntity> lRates = latestRepo.findAll();

		assertThat(hRates).hasSize(131);
		assertThat(hRates).containsOnlyOnce(expectedOldLatest, expectedFirstCallLatest, expectedAdded, expectedNewLatest);

		assertThat(lRates).hasSize(2);
		assertThat(lRates).containsOnlyOnce(expectedNewLatest);
		assertThat(lRates).doesNotContain(expectedOldLatest, expectedFirstCallLatest, expectedAdded);

		verify(bus, times(2)).post(any());
	}
}