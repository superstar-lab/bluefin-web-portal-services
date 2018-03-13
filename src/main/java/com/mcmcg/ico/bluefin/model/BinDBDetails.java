package com.mcmcg.ico.bluefin.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.mcmcg.ico.bluefin.rest.resource.Views;

import lombok.Data;

@Data
public class BinDBDetails {
	
	@JsonView({ Views.Extend.class, Views.Summary.class })
	private Long binDBId;
	@JsonView({ Views.Extend.class, Views.Summary.class })
    private String bin;
	@JsonView({ Views.Extend.class, Views.Summary.class })
    private String brand;
	@JsonView({ Views.Extend.class, Views.Summary.class })
    private String bank;
	@JsonView({ Views.Extend.class, Views.Summary.class })
    private String type;
	@JsonView({ Views.Extend.class, Views.Summary.class })
    private String level;
	@JsonView({ Views.Extend.class, Views.Summary.class })
    private String isocountry;
	@JsonView({ Views.Extend.class, Views.Summary.class })
    private String info;
	@JsonView({ Views.Extend.class, Views.Summary.class })
    @JsonProperty("country_iso")
    private String countryIso;
	@JsonView({ Views.Extend.class, Views.Summary.class })
    @JsonProperty("country2_iso")
    private String country2Iso;
	@JsonView({ Views.Extend.class, Views.Summary.class })
    @JsonProperty("country3_iso")
	private String country3Iso;
	@JsonView({ Views.Extend.class, Views.Summary.class })
    private String www;
	@JsonView({ Views.Extend.class, Views.Summary.class })
    private String phone;

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
