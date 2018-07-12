package com.mcmcg.ico.bluefin;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.web.WebApplicationInitializer;

import com.mcmcg.ico.bluefin.configuration.properties.CookiesConfiguration;

@Component
public class BluefinAppContextAware implements ApplicationContextAware,WebApplicationInitializer  {

	private static final Logger LOGGER = LoggerFactory.getLogger(BluefinAppContextAware.class);
	
	@Autowired
	private ServletContext servletContext;
	
	@Autowired
	private CookiesConfiguration cookieConfig;
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		LOGGER.info("servletContext={} , cookieConfig={}",servletContext,cookieConfig);
	}

	@Override
	public void onStartup(ServletContext servletContext) throws ServletException {
		// TODO Auto-generated method stub
		 servletContext.getSessionCookieConfig().setSecure(cookieConfig.isSecure());
	}

}
