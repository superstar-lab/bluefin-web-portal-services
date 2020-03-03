package com.mcmcg.ico.bluefin.configuration.properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CookiesConfiguration {
	@Value("${secure.cookie}")
	private boolean secure;
	
	

	public String toString(){
		return "SEC="+secure;
	}



	public boolean isSecure() {
		return secure;
	}



	public void setSecure(boolean secure) {
		this.secure = secure;
	}
	
	
}
