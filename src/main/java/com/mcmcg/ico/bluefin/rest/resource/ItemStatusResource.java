package com.mcmcg.ico.bluefin.rest.resource;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

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

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getTask() {
		return task;
	}

	public void setTask(String task) {
		this.task = task;
	}

	public List<ItemStatusCodeResource> getCodeStatus() {
		return codeStatus;
	}

	public void setCodeStatus(List<ItemStatusCodeResource> codeStatus) {
		this.codeStatus = codeStatus;
	}

	public Boolean getCompleted() {
		return completed;
	}

	public void setCompleted(Boolean completed) {
		this.completed = completed;
	}
    
    
}
