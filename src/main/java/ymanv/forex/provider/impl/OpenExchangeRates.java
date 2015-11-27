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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import ymanv.forex.model.entity.rate.RateEntity;
import ymanv.forex.provider.AProvider;
import ymanv.forex.provider.impl.oer.OpenExchangeRatesModel;

/**
 * 165 currencies, 10000 API calls/month, 1h updated rates
 */
@Component
public class OpenExchangeRates extends AProvider {

	public static final String URL = "https://openexchangerates.org/api/latest.json";

	@Value("${openexchangerates.app_id:}")
	private String appId;

	private String fullUrl;

	@PostConstruct
	private void url() {
		fullUrl = URL + "?app_id=" + appId;
	}

	private final ObjectMapper mapper = new ObjectMapper();

	@Override
	public List<RateEntity> getRates() throws IOException {
		String response = handler.sendGet(fullUrl);
		OpenExchangeRatesModel model = mapper.readValue(response, OpenExchangeRatesModel.class);

		Date rateTime = new Date(model.getTimestamp() * 1000);

		List<RateEntity> rates = new ArrayList<>();

		for (Entry<String, BigDecimal> entry : model.getRates().entrySet()) {
			String targetCurrency = entry.getKey();
			BigDecimal value = entry.getValue();

			if (!USD.equals(targetCurrency)) {
				rates.add(new RateEntity(USD, targetCurrency, value, rateTime));
			}
		}

		return rates;
	}
}
