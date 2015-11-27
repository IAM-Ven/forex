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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;

import ymanv.forex.model.entity.rate.RateEntity;
import ymanv.forex.provider.AProvider;
import ymanv.forex.provider.impl.yahoo.YahooFields;
import ymanv.forex.provider.impl.yahoo.YahooResource;
import ymanv.forex.util.StringUtils;

/**
 * Supporting 172 currencies including : <br>
 * <ul>
 * <li>Copper (XCP)</li>
 * <li>Zambian kwacha (ZMW)</li>
 * </ul>
 */
@Component
public class Yahoo extends AProvider {

	private static final Logger LOG = LoggerFactory.getLogger(Yahoo.class);

	private static final String URL = "http://finance.yahoo.com/webservice/v1/symbols/allcurrencies/quote?format=json";

	private final ObjectMapper mapper = new ObjectMapper();

	@Override
	public List<RateEntity> getRates() throws IOException {
		String response = handler.sendGet(URL);

		try {
			response = clean(response);

			CollectionType listType = mapper.getTypeFactory().constructCollectionType(List.class, YahooResource.class);
			JsonNode node = mapper.readTree(response).get("list").get("resources");
			List<YahooResource> model = mapper.readValue(node.toString(), listType);

			List<RateEntity> rates = new ArrayList<>();

			for (YahooResource o : model) {
				YahooFields yf = o.getResource().getFields();
				String symbol = yf.getSymbol();

				if (USD.equals(symbol)) {
					continue;
				}

				rates.add(new RateEntity(USD, symbol, yf.getPrice(), yf.getUtctime()));
			}

			return rates;
		} catch (Exception e) {
			LOG.error("Response: {}", StringUtils.toOneLine(response));
			throw e;
		}
	}

	/**
	 * Workaround when sometimes, for unknown reason, the response is surrounded by square brackets or other random characters.
	 * 
	 * @param response
	 * @return
	 */
	private String clean(String response) {

		String resp = response.trim();
		int startIndex = resp.indexOf("{");

		if (startIndex > 0) {
			LOG.warn("Response has to be cleaned [start]");
			resp = resp.substring(startIndex);
		}

		int endIndex = resp.lastIndexOf("}");

		if (endIndex < resp.length() - 1) {
			LOG.warn("Response has to be cleaned [end]");
			resp = resp.substring(0, endIndex + 1);
		}

		return resp;
	}
}
