package com.mcmcg.ico.bluefin;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import com.mcmcg.ico.bluefin.configuration.properties.CookiesConfiguration;

@Component
public class BluefinAppContextAware implements ApplicationContextAware {

	private static final Logger LOGGER = LoggerFactory.getLogger(BluefinAppContextAware.class);
	
	@Autowired
	private ServletContext servletContext;
	
	@Autowired
	private CookiesConfiguration cookieConfig;
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		LOGGER.info("Setting cookie setting to servlet context");
		if (servletContext != null) {
			if (servletContext.getSessionCookieConfig() != null) {
				if (cookieConfig != null) {
					LOGGER.info("The Cookie config secure , set as={}",cookieConfig.isSecure());
					try {
						servletContext.getSessionCookieConfig().setSecure(cookieConfig.isSecure());
					} catch (Exception ex) {
						LOGGER.info("Failed to set Cookie config setting , Exp Message={}",ex.getMessage(),ex);
					}
				} else {	
					LOGGER.info("The Cookie Config Object found null");
				}
			} else {
				LOGGER.error("SessionCookieConfig object from Servlet Context found null hence not setting cookie properties (secure)");
			}
		} else {
			LOGGER.error("Servlet Context found null hence not setting cookie properties (secure)");
		}
	}

}
