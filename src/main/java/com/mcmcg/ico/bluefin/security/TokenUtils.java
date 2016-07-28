package com.mcmcg.ico.bluefin.security;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.mcmcg.ico.bluefin.persistent.SecurityTokenBlacklist;
import com.mcmcg.ico.bluefin.persistent.User;
import com.mcmcg.ico.bluefin.persistent.jpa.SecurityTokenRepository;
import com.mcmcg.ico.bluefin.persistent.jpa.UserRepository;
import com.mcmcg.ico.bluefin.security.rest.resource.TokenType;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class TokenUtils {

    @Value("${bluefin.wp.services.token.secret}")
    private String secret;

    @Value("${bluefin.wp.services.token.expiration}")
    private Long expiration;

    @Value("${bluefin.wp.services.token.resetpassword.expiration}")
    private Long resetpasswordExpiration;

    @Value("${bluefin.wp.services.token.registeruser.expiration}")
    private Long registerUserExpiration;

    @Value("${bluefin.wp.services.token.authentication.expiration}")
    private Long authenticationExpiration;

    @Value("${bluefin.wp.services.token.application.expiration}")
    private Long applicationExpiration;

    @Autowired
    private SecurityTokenRepository tokenRepository;
    @Autowired
    private UserRepository userRepository;

    public String getUsernameFromToken(String token) {
        String username;
        try {
            final Claims claims = this.getClaimsFromToken(token);
            username = claims.getSubject();
        } catch (Exception e) {
            return null;
        }
        return username;
    }

    public Date getCreatedDateFromToken(String token) {
        Date created;
        try {
            final Claims claims = this.getClaimsFromToken(token);
            created = new Date((Long) claims.get("created"));
        } catch (Exception e) {
            created = null;
        }
        return created;
    }

    public String getTypeFromToken(String token) {
        String type;
        try {
            final Claims claims = this.getClaimsFromToken(token);
            type = claims.get("type").toString();
        } catch (Exception e) {
            type = null;
        }
        return type;
    }

    public String getUrlFromToken(String token) {
        String url;
        try {
            final Claims claims = this.getClaimsFromToken(token);
            url = claims.get("url").toString();
        } catch (Exception e) {
            url = null;
        }
        return url;
    }

    public Date getExpirationDateFromToken(String token) {
        Date expiration;
        try {
            final Claims claims = this.getClaimsFromToken(token);
            expiration = claims.getExpiration();
        } catch (Exception e) {
            expiration = null;
        }
        return expiration;
    }

    private Claims getClaimsFromToken(String token) {
        Claims claims;
        try {
            claims = Jwts.parser().setSigningKey(this.secret).parseClaimsJws(token).getBody();
        } catch (Exception e) {
            claims = null;
        }
        return claims;
    }

    private Date generateCurrentDate() {
        return new Date(System.currentTimeMillis());
    }

    private Date generateExpirationDate(TokenType type) {
        switch (type) {
        case AUTHENTICATION:
            return new Date(System.currentTimeMillis() + this.authenticationExpiration * 1000);
        case FORGOT_PASSWORD:
            return new Date(System.currentTimeMillis() + this.resetpasswordExpiration * 1000);
        case REGISTER_USER:
            return new Date(System.currentTimeMillis() + this.registerUserExpiration * 1000);
        case APPLICATION:
            return new Date(System.currentTimeMillis() + this.applicationExpiration * 1000);
        default:
            return new Date(System.currentTimeMillis() + this.expiration * 1000);
        }
    }

    private Boolean isTokenExpired(String token) {
        final Date expiration = this.getExpirationDateFromToken(token);
        return expiration.before(this.generateCurrentDate());
    }

    private Boolean isCreatedBeforeLastPasswordReset(Date created, Date lastPasswordReset) {
        return (lastPasswordReset != null && created.before(lastPasswordReset));
    }

    public String generateToken(UserDetails userDetails, TokenType type, String url) {
        Map<String, Object> claims = new HashMap<String, Object>();
        claims.put("sub", userDetails.getUsername());
        claims.put("created", this.generateCurrentDate());
        claims.put("type", type.name());
        claims.put("url", url);
        return this.generateToken(claims);
    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(userDetails, null, null);
    }

    private String generateToken(Map<String, Object> claims) {
        return Jwts.builder().setClaims(claims)
                .setExpiration(this.generateExpirationDate(TokenType.valueOf(claims.get("type").toString())))
                .signWith(SignatureAlgorithm.HS512, this.secret).compact();
    }

    public Boolean canTokenBeRefreshed(String token, Date lastPasswordReset) {
        final Date created = this.getCreatedDateFromToken(token);
        return (!(this.isCreatedBeforeLastPasswordReset(created, lastPasswordReset)) && (!this.isTokenExpired(token)));
    }

    public String refreshToken(String token) {
        String refreshedToken;
        try {
            final Claims claims = this.getClaimsFromToken(token);
            claims.put("created", this.generateCurrentDate());
            refreshedToken = this.generateToken(claims);
        } catch (Exception e) {
            refreshedToken = null;
        }
        return refreshedToken;
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = this.getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !this.isTokenExpired(token)
                && !isTokenInBlacklist(token, username));
    }

    public SecurityTokenBlacklist sendTokenToBlacklist(String token, String username) {
        User user = userRepository.findByUsername(username);
        SecurityTokenBlacklist blacklistToken = new SecurityTokenBlacklist();
        blacklistToken.setToken(token);
        blacklistToken.setUserId(user.getUserId());
        // TODO: multiple token types
        blacklistToken.setType(TokenType.AUTHENTICATION.name());
        return tokenRepository.save(blacklistToken);
    }

    public Boolean isTokenInBlacklist(String token, String username) {
        User user = userRepository.findByUsername(username);
        SecurityTokenBlacklist blacklistToken = tokenRepository.findByUserIdAndToken(user.getUserId(), token);
        return blacklistToken != null;
    }

}
