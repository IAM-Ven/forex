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
package ymanv.forex.util;

import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class CurrencyUtils {

	public static final String EUR = "EUR";
	public static final String USD = "USD";
	public static final String GBP = "GBP";
	public static final String CHF = "CHF";

	private static final Map<String, String> COMMODITIES = new HashMap<>();

	static {
		COMMODITIES.put("XAU", "gold");
		COMMODITIES.put("XAG", "silver");
		//COMMODITIES.put(Currency.getInstance("XPT"), "platin");
		//COMMODITIES.put(Currency.getInstance("XPD"), "palladium");
	}

	private static List<String> countryCodesForCurrency(String c) {
		Set<String> codes = new HashSet<>();

		if (isValidCode(c)) {
			for (Locale l : Locale.getAvailableLocales()) {
				try {
					Currency currentCurrency = Currency.getInstance(l);

					if (currentCurrency.getCurrencyCode().equals(c)) {
						codes.add(l.getCountry());
					}
				} catch (IllegalArgumentException e) {
				}
			}
		}

		return new ArrayList<>(codes);
	}

	public static boolean isValidCode(String cur) {
		try {
			Currency.getInstance(cur);
			return true;
		} catch (IllegalArgumentException e) {
			return false;
		}
	}

	public static String codeForCurrency(String c) {
		if (EUR.equals(c)) {
			return "europeanunion";
		} else if (USD.equals(c)) {
			return "us";
		}

		String commodityCode = COMMODITIES.get(c);

		if (commodityCode != null) {
			return commodityCode;
		}

		List<String> codes = countryCodesForCurrency(c);

		if (!codes.isEmpty()) {
			String code = codes.get(0);
			return code.toLowerCase();
		}

		return null;
	}

}
