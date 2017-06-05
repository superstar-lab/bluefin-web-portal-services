package com.mcmcg.ico.bluefin.rest.resource;

import java.io.Serializable;

import lombok.Data;

@Data
public class TokenResponse extends BasicToken implements Serializable {

    private static final long serialVersionUID = 4045833355784335236L;

    public TokenResponse(String token) {
    	super(token);
        
    }

}
