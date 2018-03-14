package com.mcmcg.ico.bluefin.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonIgnoreProperties({"countryIso","country2Iso","country3Iso"})
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
	}
}
