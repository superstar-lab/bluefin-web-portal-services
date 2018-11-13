package com.mcmcg.ico.bluefin.model;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
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
	
	public BinDBDetails getSelf(){
		return this;
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
}
