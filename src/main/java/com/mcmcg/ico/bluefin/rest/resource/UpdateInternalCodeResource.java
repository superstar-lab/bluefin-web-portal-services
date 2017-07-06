package com.mcmcg.ico.bluefin.rest.resource;

import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class UpdateInternalCodeResource extends InternalCodeResource {
    @NotNull(message = "Please provide a internal code id for the internal code")
    private Long internalCodeId;
}
