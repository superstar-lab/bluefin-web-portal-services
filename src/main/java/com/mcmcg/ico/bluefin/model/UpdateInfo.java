package com.mcmcg.ico.bluefin.model;

import lombok.Data;

@Data
public class UpdateInfo {
    String token;
    String application;
    String accountNo;
    String updateDate;
    String updateReason;

    public UpdateInfo(String token, String application, String accountNo, String updateDate) {
        this.token = token;
        this.application = application;
        this.accountNo = accountNo;
        this.updateDate = updateDate;
    }

    //This is use to deserialize endpoint body
    public UpdateInfo() {
    }
}
