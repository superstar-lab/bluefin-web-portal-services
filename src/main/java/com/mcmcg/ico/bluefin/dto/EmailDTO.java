package com.mcmcg.ico.bluefin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailDTO {

    private String subject;
    private String recipientAddress;
    private String body;

}
