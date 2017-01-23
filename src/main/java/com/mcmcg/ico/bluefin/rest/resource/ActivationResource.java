package com.mcmcg.ico.bluefin.rest.resource;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class ActivationResource implements Serializable {

    private static final long serialVersionUID = -3748256987303902432L;
    private List<String> usernames;
    private boolean activate;

}
