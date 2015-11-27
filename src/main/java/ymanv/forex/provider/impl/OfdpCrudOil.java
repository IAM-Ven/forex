/**
 * Copyright (C) 2014 https://github.com/ymanv
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

import static ymanv.forex.util.CurrencyUtils.USD;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ymanv.forex.model.entity.rate.RateEntity;
import ymanv.forex.provider.AProvider;
import ymanv.forex.provider.impl.ofdp.OfdpCrudOilModel;
import ymanv.forex.provider.impl.ofdp.OfdpRate;

@Component
public class OfdpCrudOil extends AProvider {

	public static final String WTI = "WTI";
	public static final String BRE = "BRE";

	public static final String WTI_URL = "https://www.quandl.com/api/v1/datasets/CHRIS/CME_CL1.json";
	public static final String BRE_URL = "https://www.quandl.com/api/v1/datasets/CHRIS/ICE_B1.json";

	private static final Logger LOG = LoggerFactory.getLogger(OfdpCrudOil.class);

	private final ObjectMapper mapper = new ObjectMapper();

	@Override
	public List<RateEntity> getRates() {
		throw new RuntimeException("not yet implemented");
	}

	public List<RateEntity> getBrentRates() throws JsonParseException, JsonMappingException, IOException {
		return getRates(BRE, BRE_URL);
	}

	public List<RateEntity> getWtiRates() throws JsonParseException, JsonMappingException, IOException {
		return getRates(WTI, WTI_URL);
	}

	private List<RateEntity> getRates(String code, String url) throws JsonParseException, JsonMappingException, IOException {
		String response = handler.sendGet(url);

		OfdpCrudOilModel model = mapper.readValue(response, OfdpCrudOilModel.class);

		List<RateEntity> rates = new ArrayList<>();

		List<OfdpRate> ofdpRates = model.getData();

		for (OfdpRate o : ofdpRates) {
			if (o.getOpen() == null) {
				LOG.warn("No Open data for date: {}", o.getDate());
			} else {
				rates.add(new RateEntity(code, USD, o.getOpen(), o.getDate()));
			}
		}

		return rates;
	}
}