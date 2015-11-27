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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.google.common.eventbus.EventBus;

import ymanv.forex.event.RatesUpdatedEvent;
import ymanv.forex.model.entity.rate.HistoricalRate;
import ymanv.forex.model.entity.rate.LatestRate;
import ymanv.forex.model.entity.rate.RateEntity;
import ymanv.forex.model.repositories.HistoricalRateRepository;
import ymanv.forex.model.repositories.LatestRateRepository;
import ymanv.forex.provider.AProvider;

@Service
@ConditionalOnProperty("forex.run")
public class Forex {

	private static final Logger LOG = LoggerFactory.getLogger(Forex.class);

	private final HistoricalRateRepository repo;

	private final LatestRateRepository latestrepo;

	private final AProvider defaultProvider;

	private final EventBus bus;

	@Autowired
	public Forex(HistoricalRateRepository repo, LatestRateRepository latestrepo, @Qualifier("yahoo") AProvider defaultProvider, EventBus bus) {
		this.repo = repo;
		this.latestrepo = latestrepo;
		this.defaultProvider = defaultProvider;
		this.bus = bus;
	}

	@Scheduled(fixedRateString = "${forex.interval}")
	public void updateRates() throws IOException {

		long start = System.currentTimeMillis();

		LOG.debug("Updating rates");

		List<RateEntity> rates = defaultProvider.getRates();

		long fetchEnd = System.currentTimeMillis();

		List<LatestRate> existingRates = latestrepo.findAll();

		List<HistoricalRate> newHistoRates = new ArrayList<>();
		List<LatestRate> newLatestRates = new ArrayList<>();

		for (RateEntity rate : rates) {
			LatestRate existingRate = getFromList(existingRates, rate);

			if (existingRate == null) {
				newLatestRates.add(new LatestRate(rate));
				newHistoRates.add(new HistoricalRate(rate));

			} else if (rate.getDate().after(existingRate.getDate())) {
				existingRate.setDate(rate.getDate());
				existingRate.setValue(rate.getValue());
				newLatestRates.add(existingRate);
				newHistoRates.add(new HistoricalRate(rate));
			}
		}

		latestrepo.save(newLatestRates);
		repo.save(newHistoRates);

		if (!newLatestRates.isEmpty()) {
			bus.post(new RatesUpdatedEvent(newLatestRates));
		}

		long end = System.currentTimeMillis();

		LOG.debug("Data saved in {} ms", (end - fetchEnd));

		LOG.info("Rates updated in {} ms", (end - start));
	}

	private <T extends RateEntity> T getFromList(List<T> rates, RateEntity rate) {
		for (T r : rates) {
			if (r.getFromcur().equals(rate.getFromcur()) && r.getTocur().equals(rate.getTocur())) {
				return r;
			}
		}

		return null;
	}
}