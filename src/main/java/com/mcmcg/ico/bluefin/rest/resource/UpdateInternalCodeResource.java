package com.mcmcg.ico.bluefin.rest.resource;

import javax.validation.constraints.NotNull;

public class UpdateInternalCodeResource extends InternalCodeResource {
    @NotNull(message = "Please provide a internal code id for the internal code")
    private Long internalCodeId;

	public Long getInternalCodeId() {
		return internalCodeId;
	}

	public void setInternalCodeId(Long internalCodeId) {
		this.internalCodeId = internalCodeId;
	}
    
    
}
