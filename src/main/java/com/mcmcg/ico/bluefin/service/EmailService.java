package com.mcmcg.ico.bluefin.service;

import com.mcmcg.ico.bluefin.dto.EmailDTO;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

@Service
public class EmailService {

    @Value("${gateway.payment.util.url}${gateway.payment.util.email.endpoint}")
    private String EMAIL_URL;


    @SneakyThrows
    public void sendEmail(String recipientAddress, String subject, String content) {
        RestTemplate restTemplate = new RestTemplate();

        HttpEntity<EmailDTO> request = new HttpEntity<>(
                EmailDTO
                        .builder()
                        .subject(subject)
                        .recipientAddress(recipientAddress)
                        .body(content)
                        .build()
        );

        restTemplate.postForObject(new URI(EMAIL_URL), request, String.class);

    }
}
