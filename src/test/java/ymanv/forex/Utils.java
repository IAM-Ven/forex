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
package ymanv.forex;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;

import ymanv.forex.model.entity.rate.RateEntity;

public class Utils {

	public static String readFile(String path) throws IOException, URISyntaxException {
		return new String(Files.readAllBytes(Paths.get(Utils.class.getResource(path).toURI())), StandardCharsets.UTF_8);
	}

	public static RateEntity rate(String from, String to, BigDecimal rate, Date date) {
		return new RateEntity(from, to, rate, date);
	}
}
