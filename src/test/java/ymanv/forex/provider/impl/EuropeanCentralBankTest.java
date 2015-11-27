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
package ymanv.forex.provider.impl;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import ymanv.forex.Utils;
import ymanv.forex.http.URLConnectionHandler;
import ymanv.forex.model.entity.rate.RateEntity;
import ymanv.forex.provider.impl.EuropeanCentralBank;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import static org.mockito.Mockito.doReturn;
import static ymanv.forex.util.CurrencyUtils.CHF;
import static ymanv.forex.util.CurrencyUtils.EUR;
import static ymanv.forex.util.CurrencyUtils.GBP;
import static ymanv.forex.util.CurrencyUtils.USD;

@RunWith(MockitoJUnitRunner.class)
public class EuropeanCentralBankTest {

	@InjectMocks
	private EuropeanCentralBank spied;

	@Mock
	private URLConnectionHandler handler;

	private static String MOCK_DAILY, MOCK_HIST;

	/** 2014-12-19 15:00:00 Europe/Paris */
	private static final long EPOCH_20141219_14H00S00_UTC = 1418997600000l;

	@BeforeClass
	public static void setUpBeforeClass() throws IOException, URISyntaxException {
		MOCK_DAILY = Utils.readFile("/provider/ecb/eurofxref-daily.xml");
		MOCK_HIST = Utils.readFile("/provider/ecb/eurofxref-hist-90d.xml");
	}

	@Test
	public void testGetRatesOK() throws Exception {
		Date expectedDate = new Date(EPOCH_20141219_14H00S00_UTC);

		doReturn(MOCK_DAILY).when(handler).sendGet(EuropeanCentralBank.URL_DAILY);

		RateEntity rateEurUsd = new RateEntity(EUR, USD, new BigDecimal("1.2279"), expectedDate);
		RateEntity rateEurGbp = new RateEntity(EUR, GBP, new BigDecimal("0.78470"), expectedDate);
		RateEntity rateEurChf = new RateEntity(EUR, CHF, new BigDecimal("1.2039"), expectedDate);

		List<RateEntity> r = spied.getRates();

		assertEquals(32, r.size());
		assertTrue(r.contains(rateEurUsd));
		assertTrue(r.contains(rateEurChf));
		assertTrue(r.contains(rateEurGbp));
	}

	@Test
	public void testGetHistoricalRatesOK() throws Exception {
		doReturn(MOCK_HIST).when(handler).sendGet(EuropeanCentralBank.URL_HIST);

		List<RateEntity> r = spied.getHistoricalRatesUSDBase();

		assertEquals(1922, r.size());
	}
}
