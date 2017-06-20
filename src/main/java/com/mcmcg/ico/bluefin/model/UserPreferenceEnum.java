package com.mcmcg.ico.bluefin.model;

public enum UserPreferenceEnum {
	USERTIMEZONEPREFRENCE("PRFTZ01");

    private final String type;

    private UserPreferenceEnum(final String value) {
        this.type = value;
    }

    @Override
    public String toString() {
        return type;
    }
	
}
