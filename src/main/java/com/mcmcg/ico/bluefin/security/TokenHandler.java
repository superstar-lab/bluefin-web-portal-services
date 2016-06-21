package com.mcmcg.ico.bluefin.security;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Date;

import javax.annotation.PostConstruct;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mcmcg.ico.bluefin.persistent.Token;
import com.mcmcg.ico.bluefin.persistent.jpa.TokenRepository;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomUnauthorizedException;
import com.mcmcg.ico.bluefin.security.model.SecurityUser;

@Component
public final class TokenHandler {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final String SEPARATOR = ".";
    private static final String SEPARATOR_SPLITTER = "\\.";

    private Mac hmac;

    @Value("${bluefin.wp.services.token.secret}")
    private String securityTokenSecret;

    @Autowired
    private TokenRepository tokenRepository;

    @PostConstruct
    public void init() {
        try {
            hmac = Mac.getInstance(HMAC_ALGORITHM);
            hmac.init(new SecretKeySpec(securityTokenSecret.getBytes(), HMAC_ALGORITHM));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("failed to initialize HMAC: " + e.getMessage(), e);
        }
    }

    public String validateToken(Token token) {
        String result = null;
        if (token != null) {
            // Check if token still alive or if is expire
            if (new Date().getTime() < token.getExpire().getTime()) {
                result = token.getToken();
            } else {
                tokenRepository.delete(token);
                result = null;
            }
        }
        return result;
    }

    public SecurityUser parseUserFromToken(String token) {
        try {
            final String[] parts = token.split(SEPARATOR_SPLITTER);
            final byte[] userBytes = fromBase64(parts[0]);
            final byte[] hash = fromBase64(parts[1]);

            boolean validHash = Arrays.equals(createHmac(userBytes), hash);
            if (validHash) {
                return fromJSON(userBytes);
            }
        } catch (Exception e) {
        }
        throw new CustomUnauthorizedException("Invalid authorization header. The access token is invalid.");
    }

    public String createTokenForUser(SecurityUser user) {
        byte[] userBytes = toJSON(user);
        byte[] hash = createHmac(userBytes);
        final StringBuilder sb = new StringBuilder(170);
        sb.append(toBase64(userBytes));
        sb.append(SEPARATOR);
        sb.append(toBase64(hash));
        return sb.toString();
    }

    private SecurityUser fromJSON(final byte[] userBytes) {
        try {
            return new ObjectMapper().readValue(new ByteArrayInputStream(userBytes), SecurityUser.class);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private byte[] toJSON(UserDetails user) {
        try {
            return new ObjectMapper().writeValueAsBytes(user);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    private String toBase64(byte[] content) {
        return DatatypeConverter.printBase64Binary(content);
    }

    private byte[] fromBase64(String content) {
        return DatatypeConverter.parseBase64Binary(content);
    }

    // synchronized to guard internal hmac object
    private synchronized byte[] createHmac(byte[] content) {
        return hmac.doFinal(content);
    }
}
