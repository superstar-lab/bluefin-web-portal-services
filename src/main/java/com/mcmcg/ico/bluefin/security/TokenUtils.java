package com.mcmcg.ico.bluefin.security;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.mcmcg.ico.bluefin.BluefinWebPortalConstants;
import com.mcmcg.ico.bluefin.model.SecurityTokenBlacklist;
import com.mcmcg.ico.bluefin.model.User;
import com.mcmcg.ico.bluefin.repository.SecurityTokenBlacklistDAO;
import com.mcmcg.ico.bluefin.repository.UserDAO;
import com.mcmcg.ico.bluefin.security.rest.resource.TokenType;
import com.mcmcg.ico.bluefin.service.PropertyService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class TokenUtils {
	private static final Logger LOGGER = LoggerFactory.getLogger(TokenUtils.class);
	@Autowired
	private PropertyService propertyService;
	@Autowired
	private SecurityTokenBlacklistDAO securityTokenBlacklistDAO;
	@Autowired
	private UserDAO userDAO;

	public String getUsernameFromToken(String token) {
		String username;
		try {
			final Claims claims = this.getClaimsFromToken(token);
			username = claims.getSubject();
		} catch (Exception e) {
			if ( LOGGER.isDebugEnabled() ) {
        		LOGGER.debug("Failed to get claim from token = {}",token,e);
        	}
			return null;
		}
		return username;
	}

	public Date getCreatedDateFromToken(String token) {
		Date created;
		try {
			final Claims claims = this.getClaimsFromToken(token);
			created = new Date((Long) claims.get(BluefinWebPortalConstants.CREATED));
		} catch (Exception e) {
			if ( LOGGER.isDebugEnabled() ) {
        		LOGGER.debug("Failed to get date from token = {}",token,e);
        	}
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
			if ( LOGGER.isDebugEnabled() ) {
        		LOGGER.debug("Failed to get type from token = {}",token,e);
        	}
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
			if ( LOGGER.isDebugEnabled() ) {
        		LOGGER.debug("Failed to get url from token = {}",token,e);
        	}
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
			if ( LOGGER.isDebugEnabled() ) {
        		LOGGER.debug("Failed to get expiration from token = {}",token,e);
        	}
			expiration = null;
		}
		return expiration;
	}

	private Claims getClaimsFromToken(String token) {
		Claims claims;
		try {
			claims = Jwts.parser().setSigningKey(propertyService.getPropertyValue("TOKEN_SECRET_KEY"))
					.parseClaimsJws(token).getBody();
		} catch (Exception e) {
			if ( LOGGER.isDebugEnabled() ) {
        		LOGGER.debug("Failed to get claims from token = {}",token,e);
        	}
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
			return getDate("AUTHENTICATION_TOKEN_EXPIRATION",1);
		case FORGOT_PASSWORD:
			return getDate("RESET_PASSWORD_TOKEN_EXPIRATION",1);
		case REGISTER_USER:
			return getDate("REGISTER_USER_TOKEN_EXPIRATION",1);
		case APPLICATION:
			return getDate("APPLICATION_TOKEN_EXPIRATION",0);
		case TRANSACTION:
			return getDate("TOKEN_TRANSACTION_EXPIRATION",0);			
		default:
			return getDate("TOKEN_EXPIRATION",1);
		}
	}
	
	private Date getDate(String prpName,int type){
		if (type == 1) {
			return new Date(System.currentTimeMillis() + getValueAsInt(prpName) );
		} else {
			return new Date(System.currentTimeMillis() + getValueAsLong(prpName) );
		}
	}
	
	private int getValueAsInt(String prpName){
		return Integer.parseInt(propertyService.getPropertyValue(prpName)) * 1000;
	}

	private long getValueAsLong(String prpName){
		return Long.parseLong(propertyService.getPropertyValue(prpName)) * 1000;
	}
	private Boolean isTokenExpired(String token) {
		final Date expiration = this.getExpirationDateFromToken(token);
		return expiration.before(this.generateCurrentDate());
	}

	private Boolean isCreatedBeforeLastPasswordReset(Date created, Date lastPasswordReset) {
		return lastPasswordReset != null && created.before(lastPasswordReset);
	}

	public String generateToken(UserDetails userDetails, TokenType type, String url) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("sub", userDetails.getUsername());
		claims.put(BluefinWebPortalConstants.CREATED, this.generateCurrentDate());
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
				.signWith(SignatureAlgorithm.HS512, propertyService.getPropertyValue("TOKEN_SECRET_KEY")).compact();
	}

	public Boolean canTokenBeRefreshed(String token, Date lastPasswordReset) {
		final Date created = this.getCreatedDateFromToken(token);
		return !this.isCreatedBeforeLastPasswordReset(created, lastPasswordReset) && !this.isTokenExpired(token);
	}

	public String refreshToken(String token) {
		String refreshedToken;
		try {
			final Claims claims = this.getClaimsFromToken(token);
			claims.put(BluefinWebPortalConstants.CREATED, this.generateCurrentDate());
			refreshedToken = this.generateToken(claims);
		} catch (Exception e) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Failed to refresh token {}",token,e);
			}
			refreshedToken = null;
		}
		return refreshedToken;
	}

	public Boolean validateToken(String token, UserDetails userDetails) {
		if(userDetails != null){
			final String username = this.getUsernameFromToken(token);
			return username != null && username.equalsIgnoreCase(userDetails.getUsername()) && !this.isTokenExpired(token)
					&& !isTokenInBlacklist(token, username);			
		}else{
			return false;
		}
	}

	public SecurityTokenBlacklist sendTokenToBlacklist(String token, String username) {
		User user = userDAO.findByUsername(username);
		SecurityTokenBlacklist blacklistToken = new SecurityTokenBlacklist();
		blacklistToken.setToken(token);
		blacklistToken.setUserId(user.getUserId());
		blacklistToken.setType(TokenType.AUTHENTICATION.name());
		long tokenId = securityTokenBlacklistDAO.saveSecurityTokenBlacklist(blacklistToken);
		return securityTokenBlacklistDAO.findByTokenId(tokenId);
	}

	public Boolean isTokenInBlacklist(String token, String username) {
		User user = userDAO.findByUsername(username);
		SecurityTokenBlacklist blacklistToken = securityTokenBlacklistDAO.findByUserIdAndToken(user.getUserId(), token);
		return blacklistToken != null;
	}
}
