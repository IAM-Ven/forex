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
import static org.mockito.Mockito.doReturn;

import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import ymanv.forex.Utils;
import ymanv.forex.http.URLConnectionHandler;
import ymanv.forex.provider.impl.OfdpCrudOil;

@RunWith(MockitoJUnitRunner.class)
public class OfdpCrudOilTest {

	@InjectMocks
	private OfdpCrudOil spied;

	@Mock
	private URLConnectionHandler handler;

	private static String MOCK_BRENT, MOCK_WTI;

	@BeforeClass
	public static void setUpBeforeClass() throws IOException, URISyntaxException {
		MOCK_BRENT = Utils.readFile("/provider/ofdp/brent.json");
		MOCK_WTI = Utils.readFile("/provider/ofdp/wti.json");
	}

	@Test
	public void testGetWtiRates() throws Exception {
		doReturn(MOCK_WTI).when(handler).sendGet(OfdpCrudOil.WTI_URL);

		assertThat(spied.getWtiRates()).hasSize(8031);
	}

	@Test
	public void testGetBrentRates() throws Exception {
		doReturn(MOCK_BRENT).when(handler).sendGet(OfdpCrudOil.BRE_URL);

		assertThat(spied.getBrentRates()).hasSize(6273);
	}

}
