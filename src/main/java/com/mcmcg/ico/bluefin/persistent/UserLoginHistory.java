package com.mcmcg.ico.bluefin.persistent;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
@Entity
@Table(name = "User_Login_History")
public class UserLoginHistory implements Serializable {
    private static final long serialVersionUID = -1383201107825800748L;

    public static enum MessageCode {
        SUCCESS(1), ERROR_USER_NOT_FOUND(2), ERROR_PASSWORD_NOT_FOUND(3);

        private final Integer messageCode;

        private MessageCode(Integer messageCode) {
            this.messageCode = messageCode;
        }

        public Integer getValue() {
            return this.messageCode;
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "UserLoginHistoryID")
    private Long userLoginHistoryId;

    @Column(name = "UserID", nullable = true)
    private Long userId;

    @Column(name = "MessageID")
    private Integer messageId;

    @Column(name = "UserName")
    private String username;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Column(name = "LoginDateTime")
    private Date loginDateTime = new Date();

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Column(name = "DateCreated", insertable = false)
    private Date createdDate;
}
