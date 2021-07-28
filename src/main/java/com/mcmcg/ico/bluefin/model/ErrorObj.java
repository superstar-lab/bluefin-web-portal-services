package com.mcmcg.ico.bluefin.model;

import lombok.Data;

@Data
public class ErrorObj {
	private long timestamp;
    private String message;
    private String exception; 
}
