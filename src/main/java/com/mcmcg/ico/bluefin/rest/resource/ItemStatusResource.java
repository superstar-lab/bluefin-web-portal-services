package com.mcmcg.ico.bluefin.rest.resource;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class ItemStatusResource {

    private Integer id;
    private String task;
    private List<ItemStatusCodeResource> codeStatus;
    private Boolean completed;

    public ItemStatusResource() {
    	// Default Constructor
    }

    public ItemStatusResource(Integer id, String task, Boolean completed) {
        this.id = id;
        this.task = task;
        this.completed = completed;
    }

    public ItemStatusResource(Integer id, String task, List<ItemStatusCodeResource> codeStatus, Boolean completed) {
        this.id = id;
        this.task = task;
        this.codeStatus = codeStatus;
        this.completed = completed;
    }
}
