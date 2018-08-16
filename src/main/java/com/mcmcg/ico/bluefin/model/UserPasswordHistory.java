package com.mcmcg.ico.bluefin.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data
public class UserPasswordHistory extends Common implements Serializable {

	private static final long serialVersionUID = -8557780879103604568L;
	
	private Long passwordHistoryID;
	private Long userId;
	@JsonIgnore
	private String previousPassword;
}
