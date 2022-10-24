package com.mcmcg.ico.bluefin.model;

import lombok.Data;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UpdateInfo that = (UpdateInfo) o;
        return token.equals(that.token);
    }

    @Override
    public int hashCode() {
        return Objects.hash(token);
    }
}
