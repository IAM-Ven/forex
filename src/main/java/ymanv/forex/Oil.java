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

import static ymanv.forex.provider.impl.OfdpCrudOil.BRE;
import static ymanv.forex.util.CurrencyUtils.USD;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;

import ymanv.forex.model.entity.rate.QLatestRate;
import ymanv.forex.event.RatesUpdatedEvent;
import ymanv.forex.model.entity.rate.HistoricalRate;
import ymanv.forex.model.entity.rate.LatestRate;
import ymanv.forex.model.entity.rate.RateEntity;
import ymanv.forex.model.repositories.HistoricalRateRepository;
import ymanv.forex.model.repositories.LatestRateRepository;
import ymanv.forex.provider.impl.OfdpCrudOil;

@Service
@ConditionalOnProperty("forex.run")
public class Oil {

	private static final Logger LOG = LoggerFactory.getLogger(Oil.class);

	private final HistoricalRateRepository repo;

	private final LatestRateRepository latestrepo;

	private final OfdpCrudOil defaultProvider;

	private final EventBus bus;

	@Autowired
	public Oil(HistoricalRateRepository repo, LatestRateRepository latestrepo, OfdpCrudOil defaultProvider, EventBus bus) {
		this.repo = repo;
		this.latestrepo = latestrepo;
		this.defaultProvider = defaultProvider;
		this.bus = bus;
	}

	@Scheduled(fixedRateString = "${oil.interval}")
	public void updateRates() throws IOException {

		long start = System.currentTimeMillis();

		LOG.debug("Updating rates");

		List<RateEntity> rates = defaultProvider.getBrentRates();

		long fetchEnd = System.currentTimeMillis();

		QLatestRate qRe = QLatestRate.latestRate;
		final LatestRate oldLatestRate = latestrepo.findOne(qRe.fromcur.eq(BRE).and(qRe.tocur.eq(USD)));

		List<HistoricalRate> newHistoRates = new ArrayList<>();
		LatestRate newLatestRate = null;

		for (RateEntity rate : rates) {
			if (oldLatestRate == null || rate.getDate().after(oldLatestRate.getDate())) {
				newHistoRates.add(new HistoricalRate(rate));

				if (newLatestRate == null || rate.getDate().after(newLatestRate.getDate())) {
					newLatestRate = new LatestRate(rate);
				}
			}
		}

		repo.save(newHistoRates);

		if (newLatestRate != null) {
			if (oldLatestRate == null) {
				latestrepo.save(newLatestRate);
			} else {
				oldLatestRate.setDate(newLatestRate.getDate());
				oldLatestRate.setValue(newLatestRate.getValue());
				latestrepo.save(oldLatestRate);
			}

			bus.post(new RatesUpdatedEvent(Lists.newArrayList(newLatestRate)));
		}

		long end = System.currentTimeMillis();

		LOG.debug("Data saved in {} ms", (end - fetchEnd));

		LOG.info("Rates updated in {} ms", (end - start));
	}
}