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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static ymanv.forex.util.CurrencyUtils.EUR;
import static ymanv.forex.util.CurrencyUtils.USD;
import static ymanv.forex.util.DateUtils.DATE_TIME_WITH_TZ;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import ymanv.forex.Utils;
import ymanv.forex.http.URLConnectionHandler;
import ymanv.forex.model.entity.rate.RateEntity;

@RunWith(MockitoJUnitRunner.class)
public class OpenExchangeRatesTest {

	@InjectMocks
	private OpenExchangeRates provider;

	@Mock
	private URLConnectionHandler handler;

	private static String MOCK_LATEST;

	@BeforeClass
	public static void setUpBeforeClass() throws IOException, URISyntaxException {
		MOCK_LATEST = Utils.readFile("/provider/oer/latest.json");
	}

	@Test
	public void testGetRatesOK() throws IOException, Exception {
		RateEntity expectedRate = new RateEntity(USD, EUR, new BigDecimal("0.882016"), DATE_TIME_WITH_TZ.parse("2015-09-13 14:00:10.0 CEST"));

		doReturn(MOCK_LATEST).when(handler).sendGet(anyString());

		assertThat(provider.getRates()).hasSize(170).containsOnlyOnce(expectedRate);
	}
}
