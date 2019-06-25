package com.mcmcg.ico.bluefin.model;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties({"countryIso","country2Iso","country3Iso","self"})
public class BinDBDetails {
	
	private Long binDBId;
	private String bin;
	private String brand;
	private String bank;
	private String type;
	private String level;
	private String isocountry;
	private String info;
	private String www;
	private String phone;
    private String countryIso;
	private String country2Iso;
	private String country3Iso;
	private BinDBDetails self;
	public BinDBDetails(){
		this.binDBId = null;
		this.bin = "";
		this.brand = "";
		this.bank = "";
		this.type = "";
		this.level = "";
		this.isocountry = "";
		this.info = "";
		this.countryIso = "";
		this.country2Iso = "";
		this.country3Iso = "";
		this.www = "";
		this.phone = "";
		this.self = null;
	}
	
	/**
	 * This method update blank and NULL values to "" (Without any space - Blank)
	 * Bin , Type value can not null in database table.
	 */
	public void updateNullValuesToBlank() {
		if (StringUtils.isBlank(this.brand) || StringUtils.equalsIgnoreCase("NULL",this.brand)) {
			this.brand = "";
		}
		if (StringUtils.isBlank(this.bank) || StringUtils.equalsIgnoreCase("NULL",this.bank)) {
			this.bank = "";
		}
		if (StringUtils.isBlank(this.level) || StringUtils.equalsIgnoreCase("NULL",this.level)) {
			this.level = "";
		}
		
		updateNullValuesToBlank1();
	}
	
	public void updateNullValuesToBlank1() {
		if (StringUtils.isBlank(this.isocountry) || StringUtils.equalsIgnoreCase("NULL",this.isocountry)) {
			this.isocountry = "";
		}
		if (StringUtils.isBlank(this.info) || StringUtils.equalsIgnoreCase("NULL",this.info)) {
			this.info = "";
		}
		if (StringUtils.isBlank(this.www) || StringUtils.equalsIgnoreCase("NULL",this.www)) {
			this.www = "";
		}
		if (StringUtils.isBlank(this.phone) || StringUtils.equalsIgnoreCase("NULL",this.phone)) {
			this.phone = "";
		}
	}

	public Long getBinDBId() {
		return binDBId;
	}

	public void setBinDBId(Long binDBId) {
		this.binDBId = binDBId;
	}

	public String getBin() {
		return bin;
	}

	public void setBin(String bin) {
		this.bin = bin;
	}

	public String getBrand() {
		return brand;
	}

	public void setBrand(String brand) {
		this.brand = brand;
	}

	public String getBank() {
		return bank;
	}

	public void setBank(String bank) {
		this.bank = bank;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public String getIsocountry() {
		return isocountry;
	}

	public void setIsocountry(String isocountry) {
		this.isocountry = isocountry;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public String getWww() {
		return www;
	}

	public void setWww(String www) {
		this.www = www;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getCountryIso() {
		return countryIso;
	}

	public void setCountryIso(String countryIso) {
		this.countryIso = countryIso;
	}

	public String getCountry2Iso() {
		return country2Iso;
	}

	public void setCountry2Iso(String country2Iso) {
		this.country2Iso = country2Iso;
	}

	public String getCountry3Iso() {
		return country3Iso;
	}

	public void setCountry3Iso(String country3Iso) {
		this.country3Iso = country3Iso;
	}

	public BinDBDetails getSelf() {
		return self;
	}

	public void setSelf(BinDBDetails self) {
		this.self = self;
	}
	
	
}
