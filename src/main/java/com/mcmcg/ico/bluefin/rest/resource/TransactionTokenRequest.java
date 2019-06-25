package com.mcmcg.ico.bluefin.rest.resource;

import java.io.Serializable;

public class TransactionTokenRequest extends BasicToken implements Serializable {

    private static final long serialVersionUID = 4045833355784335236L;

    public TransactionTokenRequest(String token) {
    	super(token);
    }

}
