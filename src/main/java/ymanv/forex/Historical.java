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
package ymanv.forex;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import ymanv.forex.model.entity.rate.HistoricalRate;
import ymanv.forex.model.entity.rate.RateEntity;
import ymanv.forex.model.repositories.HistoricalRateRepository;
import ymanv.forex.provider.impl.EuropeanCentralBank;
import ymanv.forex.provider.impl.OfdpCrudOil;

@Service
@ConditionalOnProperty("forex.history")
public class Historical {

	private static final Logger LOG = LoggerFactory.getLogger(Historical.class);

	private HistoricalRateRepository repo;

	private OfdpCrudOil ofdp;

	private EuropeanCentralBank ecb;

	@Autowired
	public Historical(HistoricalRateRepository repo, OfdpCrudOil ofdp, EuropeanCentralBank ecb) {
		this.repo = repo;
		this.ofdp = ofdp;
		this.ecb = ecb;
	}

	@PostConstruct
	public void addHistory() throws JsonParseException, JsonMappingException, IOException {
		addBrent();
		addEcb();
	}

	private void addEcb() throws IOException {
		long start = System.currentTimeMillis();

		LOG.info("Adding historical rates from {}", ecb);

		save(ecb.getHistoricalRatesUSDBase());

		long end = System.currentTimeMillis();

		LOG.info("Rates from {} added in {} ms", ecb, (end - start));
	}

	private void addBrent() throws JsonParseException, JsonMappingException, IOException {
		long start = System.currentTimeMillis();

		LOG.debug("Adding historical rates from {}", ofdp);

		save(ofdp.getBrentRates());

		long end = System.currentTimeMillis();

		LOG.info("Rates from {} added in {} ms", ofdp, (end - start));
	}

	private void save(List<RateEntity> rates) {
		List<HistoricalRate> newHistoRates = new ArrayList<>();

		for (RateEntity r : rates) {
			newHistoRates.add(new HistoricalRate(r));
		}

		repo.save(newHistoRates);
	}
}