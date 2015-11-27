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
package ymanv.forex.provider;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import ymanv.forex.http.URLConnectionHandler;
import ymanv.forex.model.entity.rate.RateEntity;

public abstract class AProvider {

	@Autowired
	protected URLConnectionHandler handler;

	protected final static String CURRENCY_NOT_SUPPORTED_MSG = "Currency not supported: {}";

	private static final BigDecimal PRECISION_QUANTITY = new BigDecimal("1000000000");
	private static final BigDecimal ONE = new BigDecimal("1");
	private static final BigDecimal TWO = new BigDecimal("2");

	protected final static String DATE_FORMAT_STRNG = "yyyy-MM-dd";

	/**
	 * Returns the latest possible rates value (depending of the implementation).
	 * 
	 * @return List of latest rates
	 * @throws IOException
	 */
	public abstract List<RateEntity> getRates() throws IOException;

	protected static BigDecimal divide(BigDecimal dividend, BigDecimal divisor) {
		return dividend.multiply(PRECISION_QUANTITY).divide(divisor, RoundingMode.HALF_UP).divide(PRECISION_QUANTITY);
	}

	protected BigDecimal avg(BigDecimal first, BigDecimal second) {
		return first.add(second).divide(TWO);
	}

	/**
	 * Divide 1 by the specified BigDecimal.
	 * 
	 * @param d
	 * @return the divided BigDecimal
	 */
	public static BigDecimal invert(BigDecimal d) {
		return divide(ONE, d);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}
