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
package ymanv.forex.model.entity.rate;

import static java.util.Objects.requireNonNull;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.google.common.base.MoreObjects;

@JsonInclude(Include.NON_NULL)
@MappedSuperclass
public class RateEntity {

	@JsonIgnore
	@Id
	@Column(name = "id", columnDefinition = "INT")
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@NotNull
	@Column(columnDefinition = "varchar(3)")
	private String fromcur;

	@NotNull
	@Column(columnDefinition = "varchar(3)")
	private String tocur;

	@NotNull
	@Column(precision = 20, scale = 10)
	private BigDecimal value;

	@NotNull
	@Temporal(TemporalType.TIMESTAMP)
	private Date date;

	@Transient
	private String countryCodeFrom;

	@Transient
	private String countryCodeTo;

	private String fromName;

	private String toName;

	public RateEntity() {
	}

	public RateEntity(String from, String to, BigDecimal rate, Date date) {
		this.fromcur = requireNonNull(from, "from is null");
		this.tocur = requireNonNull(to, "to is null");
		this.value = requireNonNull(rate, "rate is null");
		this.date = requireNonNull(date, "date is null");
	}

	public Long getId() {
		return id;
	}

	public String getFromcur() {
		return fromcur;
	}

	public String getTocur() {
		return tocur;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public BigDecimal getValue() {
		return value;
	}

	public void setValue(BigDecimal value) {
		this.value = value;
	}

	public String getCountryCodeFrom() {
		return countryCodeFrom;
	}

	public void setCountryCodeFrom(String countryCodeFrom) {
		this.countryCodeFrom = countryCodeFrom;
	}

	public String getCountryCodeTo() {
		return countryCodeTo;
	}

	public void setCountryCodeTo(String countryCodeTo) {
		this.countryCodeTo = countryCodeTo;
	}

	public String getFromName() {
		return fromName;
	}

	public void setFromName(String fromName) {
		this.fromName = fromName;
	}

	public String getToName() {
		return toName;
	}

	public void setToName(String toName) {
		this.toName = toName;
	}

	@Override
	public int hashCode() {
		return Objects.hash(date, fromcur, tocur, value);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (obj == null || !(obj instanceof RateEntity))
			return false;

		RateEntity other = (RateEntity) obj;

		return date.compareTo(other.date) == 0 //
				&& Objects.equals(fromcur, other.fromcur) //
				&& Objects.equals(tocur, other.tocur) //
				&& value.compareTo(other.value) == 0;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this) //
				.add("date", date) //
				.add("fromcur", fromcur) //
				.add("tocur", tocur) //
				.add("value", value).toString();
	}
}
