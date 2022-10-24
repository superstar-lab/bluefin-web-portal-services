package com.mcmcg.ico.bluefin;

import javax.servlet.MultipartConfigElement;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;
import org.springframework.util.unit.DataSize;

@SpringBootApplication
@EnableAutoConfiguration
public class BluefinServicesApplication {

    @Value("${spring.datasource.bluefin.jndi-name}")
    private String bluefinJndiName;

    @Value("${spring.datasource.binddb.jndi-name}")
    private String bindbJndiName;

	@Value("${spring.datasource.blueAccountValidation.jndi-name:jdbc/ACH}")
	private String blueAccountValidationJndiName;
    
    @Value("${spring.bluefin.muti.file.upload.size}")
    private DataSize multiFileSize;

    private JndiDataSourceLookup lookup = new JndiDataSourceLookup();
    
    public static void main(String[] args) {
        SpringApplication.run(BluefinServicesApplication.class, args);
    }
    
	@Primary()
	@Bean(name = BluefinWebPortalConstants.BLUEFIN_WEB_PORTAL_DATA_SOURCE,destroyMethod = "")
	public DataSource bluefinDataSource() {
		return lookup.getDataSource(bluefinJndiName);
	}
	
	@Bean(name = BluefinWebPortalConstants.BLUEFIN_WEB_PORTAL_JDBC_TEMPLATE)
	JdbcTemplate bluefinJdbcTemplate(@Qualifier(value=BluefinWebPortalConstants.BLUEFIN_WEB_PORTAL_DATA_SOURCE)  DataSource bluefinDataSource){
		return new JdbcTemplate(bluefinDataSource);
	}
	
	@Bean(name = BluefinWebPortalConstants.BLUEFIN_BIN_DB_DATA_SOURCE,destroyMethod = "")
	public DataSource binDBDataSource() {
		return lookup.getDataSource(bindbJndiName);
	}


	@Bean(name = BluefinWebPortalConstants.BLUEFIN_ACCOUNT_VALIDATION_DATA_SOURCE,destroyMethod = "")
	public DataSource bluefinAccountValidationDataSource() {
		return lookup.getDataSource(blueAccountValidationJndiName);
	}

	@Bean(name = BluefinWebPortalConstants.BLUEFIN_ACCOUNT_VALIDATION_JDBC_TEMPLATE)
	JdbcTemplate bluefinAccountValidationJdbcTemplate(@Qualifier(value=BluefinWebPortalConstants.BLUEFIN_ACCOUNT_VALIDATION_DATA_SOURCE)  DataSource bluefinAccountValidationDataSource){
		return new JdbcTemplate(bluefinAccountValidationDataSource);
	}

	/**@Bean(name = BluefinWebPortalConstants.BLUEFIN_BIN_DB_JDBC_TEMPLATE)
	JdbcTemplate binJdbcTemplate(@Qualifier(BluefinWebPortalConstants.BLUEFIN_BIN_DB_DATA_SOURCE)  DataSource binDBDataSource){
		JdbcTemplate jdbcTemplate = new JdbcTemplate(binDBDataSource);
		return jdbcTemplate;
	}*/
	
	@Bean(name = BluefinWebPortalConstants.BLUEFIN_BIN_DB_JDBC_TEMPLATE)
	NamedParameterJdbcTemplate binNamedJdbcTemplate(@Qualifier(BluefinWebPortalConstants.BLUEFIN_BIN_DB_DATA_SOURCE)  DataSource binDBDataSource){
		return new NamedParameterJdbcTemplate(binDBDataSource);
	}
	
	@Primary()
	@Bean(name = BluefinWebPortalConstants.BLUEFIN_NAMED_JDBC_TEMPLATE)
	NamedParameterJdbcTemplate bluefinNamedJdbcTemplate(@Qualifier(BluefinWebPortalConstants.BLUEFIN_WEB_PORTAL_DATA_SOURCE)  DataSource bluefinJdbcTemplate){
		return new NamedParameterJdbcTemplate(bluefinJdbcTemplate);
	}
	
	@Bean
	public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setMaxFileSize(multiFileSize);
        factory.setMaxRequestSize(multiFileSize);
        return factory.createMultipartConfig();
    }
}
