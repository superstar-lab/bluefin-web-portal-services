package com.mcmcg.ico.bluefin.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mcmcg.ico.bluefin.ws.client.EmailRequestorWsConsumer;
import com.mcmcg.mail.dataobject.MailData;

@Service
public class EmailService {

    @Autowired
    EmailRequestorWsConsumer emailRequestorWsConsumer;

    public void sendEmail(String recipientAddress, String subject, String content) {
        // Send message to email
        MailData data = new MailData();
        data.setSubject(subject);
        data.setContent(content);
        data.setToAddress(recipientAddress);
        data.setFromAddress("no-reply@mcmcg.com");
        emailRequestorWsConsumer.dropMessage(data);
    }
}
