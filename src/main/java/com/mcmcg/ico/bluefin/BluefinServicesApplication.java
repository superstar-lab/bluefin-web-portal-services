package com.mcmcg.ico.bluefin;

import javax.servlet.MultipartConfigElement;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;

@SpringBootApplication
public class BluefinServicesApplication {

    public static void main(String[] args) {
        SpringApplication.run(BluefinServicesApplication.class, args);
    }
    
    @Value("${spring.datasource.bluefin.jndi-name}")
    private String bluefinJndiName;

    @Value("${spring.datasource.binddb.jndi-name}")
    private String bindbJndiName;
    
    @Value("${spring.bluefin.muti.file.upload.size}")
    private String multiFileSize;

    private JndiDataSourceLookup lookup = new JndiDataSourceLookup();
    
	@Primary()
	@Bean(name = BluefinWebPortalConstants.BLUEFIN_WEB_PORTAL_DATA_SOURCE,destroyMethod = "")
	public DataSource bluefinDataSource() {
		return lookup.getDataSource(bluefinJndiName);
	}
	
	@Bean(name = BluefinWebPortalConstants.BLUEFIN_WEB_PORTAL_JDBC_TEMPLATE)
	JdbcTemplate bluefinJdbcTemplate(@Qualifier(value=BluefinWebPortalConstants.BLUEFIN_WEB_PORTAL_DATA_SOURCE)  DataSource bluefinDataSource){
		JdbcTemplate jdbcTemplate = new JdbcTemplate(bluefinDataSource);
		return jdbcTemplate;
	}
	
	@Bean(name = BluefinWebPortalConstants.BLUEFIN_BIN_DB_DATA_SOURCE,destroyMethod = "")
	public DataSource binDBDataSource() {
		return lookup.getDataSource(bindbJndiName);
	}
	
	/*@Bean(name = BluefinWebPortalConstants.BLUEFIN_BIN_DB_JDBC_TEMPLATE)
	JdbcTemplate binJdbcTemplate(@Qualifier(BluefinWebPortalConstants.BLUEFIN_BIN_DB_DATA_SOURCE)  DataSource binDBDataSource){
		JdbcTemplate jdbcTemplate = new JdbcTemplate(binDBDataSource);
		return jdbcTemplate;
	}*/
	
	@Bean(name = BluefinWebPortalConstants.BLUEFIN_BIN_DB_JDBC_TEMPLATE)
	NamedParameterJdbcTemplate binNamedJdbcTemplate(@Qualifier(BluefinWebPortalConstants.BLUEFIN_BIN_DB_DATA_SOURCE)  DataSource binDBDataSource){
		NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(binDBDataSource);
		return namedParameterJdbcTemplate;
	}
	
	@Primary()
	@Bean(name = BluefinWebPortalConstants.BLUEFIN_NAMED_JDBC_TEMPLATE)
	NamedParameterJdbcTemplate bluefinNamedJdbcTemplate(@Qualifier(BluefinWebPortalConstants.BLUEFIN_WEB_PORTAL_DATA_SOURCE)  DataSource bluefinJdbcTemplate){
		NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(bluefinJdbcTemplate);
		return namedParameterJdbcTemplate;
	}
	
	@Bean
	public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setMaxFileSize(multiFileSize);
        factory.setMaxRequestSize(multiFileSize);
        return factory.createMultipartConfig();
    }
}
