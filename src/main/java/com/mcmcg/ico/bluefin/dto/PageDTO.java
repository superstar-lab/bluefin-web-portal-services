package com.mcmcg.ico.bluefin.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class PageDTO {
    @NotNull
    private Integer page;
    @NotNull
    private Integer size;

}
