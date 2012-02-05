package org.webguitoolkit.persistence.test.testobjects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class EmbeddedObject {

	@Column
	private String street;
	@Column
	private String city;
	@Column
	private String country;

	public String getCity() {
		return city;
	}

	public String getStreet() {
		return street;
	}

	public String getCountry() {
		return country;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public void setStreet(String street) {
		this.street = street;
	}

}
