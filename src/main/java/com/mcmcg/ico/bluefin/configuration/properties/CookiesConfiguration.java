package com.mcmcg.ico.bluefin.configuration.properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
public class CookiesConfiguration {
	@Value("${secure.cookie}")
	private boolean secure;
	
	

	public String toString(){
		return "SEC="+secure;
	}
}
