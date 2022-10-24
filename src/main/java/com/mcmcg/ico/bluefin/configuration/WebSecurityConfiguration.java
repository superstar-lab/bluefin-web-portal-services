package com.mcmcg.ico.bluefin.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.StaticHeadersWriter;

import com.mcmcg.ico.bluefin.BluefinWebPortalConstants;
import com.mcmcg.ico.bluefin.rest.controller.exception.ApplicationGenericException;
import com.mcmcg.ico.bluefin.security.AuthenticationTokenFilter;
import com.mcmcg.ico.bluefin.security.CustomAccessDeniedHandler;
import com.mcmcg.ico.bluefin.security.EntryPointUnauthorizedHandler;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    private EntryPointUnauthorizedHandler unauthorizedHandler;

    @Autowired
    private CustomAccessDeniedHandler accessDeniedHandler;

    @Autowired
    private UserDetailsService userDetailsService;

	@Autowired
	@Qualifier("authenticationProvider")
	DaoAuthenticationProvider authenticationProvider;
    
    @Value(("${csp.header}"))
    private String cspHeader;

	@Autowired
	public void configureAuthentication(AuthenticationManagerBuilder authenticationManagerBuilder) throws ApplicationGenericException {
		try {
			authenticationManagerBuilder.userDetailsService(this.userDetailsService).passwordEncoder(passwordEncoder());
		} catch (Exception e) {
			throw new ApplicationGenericException(e);
		}
	}

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		authenticationProvider.setPasswordEncoder(passwordEncoder());
		auth.authenticationProvider(authenticationProvider);
	}

	@Bean
	public UserDetailsService userDetails() {
		return this.userDetailsService;
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public AuthenticationTokenFilter authenticationTokenFilterBean() throws Exception {
        AuthenticationTokenFilter authenticationTokenFilter = new AuthenticationTokenFilter();
        authenticationTokenFilter.setAuthenticationManager(authenticationManagerBean());
        return authenticationTokenFilter;
    }


    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        final String apiBaseURL = "/api";
        final String transactionsApiBaseURL = apiBaseURL + "/transactions";
		final String declinedTransactionsApiBaseURL = transactionsApiBaseURL + "/generateDeclinedReport";
		final String topTranSummaryApiBaseURL = transactionsApiBaseURL + "/generateTopSummaryReport";
		final String approvedTranSummaryApiBaseURL = transactionsApiBaseURL + "/generateApprovedReport";
		final String transUpdatesApiBaseURL = transactionsApiBaseURL + "/update";
		final String transUpdatesMetricsApiBaseURL = transUpdatesApiBaseURL + "/metrics";
		final String transUpdateApiBaseURL = transUpdatesApiBaseURL + "/{\\d+}";
        final String transactionTypesApiBaseURL = apiBaseURL + "/transaction-types";
        final String sessionApiBaseURL = apiBaseURL + "/session";
        final String usersApiBaseURL = apiBaseURL + "/users";
        final String legalEntitiesApiBaseURL = apiBaseURL + "/legal-entities";
        final String reconciliationStatusApiBaseURL = apiBaseURL + "/reconciliation-status";
        final String rolesApiBaseURL = apiBaseURL + "/roles";
		final String rolesAssignPerApiBaseURL = rolesApiBaseURL + "/assign-permissions";
        final String internalResponseCodesApiBaseURL = apiBaseURL + "/internal-response-codes";
        final String paymentProcessorApiBaseURL = apiBaseURL + "/payment-processors";
        final String paymentProcessorRulesApiBaseURL = apiBaseURL + "/payment-processor-rules";
        final String paymentProcessorRemittanceApiBaseURL = apiBaseURL + "/payment-processor-remittances";
        final String internalStatusCodesApiBaseURL = apiBaseURL + "/internal-status-codes";
        final String reportsApiBaseURL = apiBaseURL + "/reports";
        final String batchUploadApiBaseURL = apiBaseURL + "/batch-upload";
        final String applicationPropertyApiBaseURL = apiBaseURL + "/applicationProperties";
		final String merchantApiBaseURL = apiBaseURL + "/merchant";

        /**CSP Code Starts Here*/
        cspHeader = cspHeader.replaceAll("\\s+", " ");
        httpSecurity.headers().addHeaderWriter(new StaticHeadersWriter("Content-Security-Policy",
        		"script-src 'self' 'unsafe-inline' 'unsafe-eval' " + cspHeader + " ; object-src 'self'" ));
        
        httpSecurity.headers().httpStrictTransportSecurity().includeSubDomains(true).maxAgeInSeconds(86400);

		httpSecurity.csrf().disable().authorizeRequests()
				.antMatchers(HttpMethod.POST,sessionApiBaseURL, sessionApiBaseURL + "/")
				.authenticated()
				.and()
				.httpBasic();

		httpSecurity.csrf().disable().exceptionHandling().accessDeniedHandler(this.accessDeniedHandler)
				.authenticationEntryPoint(this.unauthorizedHandler).and().sessionManagement()
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS).and().authorizeRequests()
				.antMatchers(HttpMethod.OPTIONS, "/**").permitAll().antMatchers(HttpMethod.GET, "/").permitAll()
				.antMatchers(HttpMethod.GET, "**/*.html", "**/*.css", "**/*.js", "**/*.ico", "/assets/**", "/csrf/**").permitAll()

				// Swagger
				.antMatchers("/swagger-ui/index.html","/swagger-ui/**", "/webjars/**", "/swagger-resources/**",
						"/v2/api-docs","/v3/api-docs", "/configuration/**", "/images/**","/csrf/**")
				.permitAll()

				.and().authorizeRequests()

				// Health
				.antMatchers(HttpMethod.GET, apiBaseURL + "/ping/", apiBaseURL + "/ping").permitAll()

				// Transactions
				.antMatchers(HttpMethod.GET, declinedTransactionsApiBaseURL, declinedTransactionsApiBaseURL + "/")
				.hasAnyAuthority(BluefinWebPortalConstants.ADMINISTRATIVE)
				.antMatchers(HttpMethod.GET, topTranSummaryApiBaseURL, topTranSummaryApiBaseURL + "/")
				.hasAnyAuthority(BluefinWebPortalConstants.ADMINISTRATIVE)
				.antMatchers(HttpMethod.GET, approvedTranSummaryApiBaseURL, approvedTranSummaryApiBaseURL + "/")
				.hasAnyAuthority(BluefinWebPortalConstants.ADMINISTRATIVE)
				.antMatchers(HttpMethod.POST, transUpdatesApiBaseURL, transUpdatesApiBaseURL + "/")
				.hasAnyAuthority(BluefinWebPortalConstants.ADMINISTRATIVE)
				.antMatchers(HttpMethod.POST, transUpdatesMetricsApiBaseURL, transUpdatesMetricsApiBaseURL + "/")
				.hasAnyAuthority(BluefinWebPortalConstants.ADMINISTRATIVE)
				.antMatchers(HttpMethod.GET, transUpdateApiBaseURL, transUpdateApiBaseURL + "/")
				.hasAnyAuthority(BluefinWebPortalConstants.ADMINISTRATIVE)
				.antMatchers(HttpMethod.GET, transactionsApiBaseURL, transactionsApiBaseURL + "/",
						transactionsApiBaseURL + "/{transactionId}", transactionsApiBaseURL + "/{transactionId}/")
				.hasAnyAuthority("SEARCH_REPORTING")

				// Transaction Types
				.antMatchers(HttpMethod.GET, transactionTypesApiBaseURL, transactionTypesApiBaseURL + "/")
				.authenticated()

				// Reports
				.antMatchers(HttpMethod.GET, reportsApiBaseURL + "/transactions", reportsApiBaseURL + "/transactions/")
				.hasAnyAuthority("SEARCH_REPORTING")
				.antMatchers(HttpMethod.GET, reportsApiBaseURL + "/batch-uploads",
						reportsApiBaseURL + "/batch-uploads/")
				.hasAnyAuthority(BluefinWebPortalConstants.BATCHREPORTING)
				.antMatchers(HttpMethod.GET, reportsApiBaseURL + "/batch-upload-transactions",
						reportsApiBaseURL + "/batch-upload-transactions/")
				.hasAnyAuthority(BluefinWebPortalConstants.BATCHREPORTING)
				.antMatchers(HttpMethod.GET, reportsApiBaseURL + "/users", reportsApiBaseURL + "/users/")
				.hasAnyAuthority(BluefinWebPortalConstants.ADMINISTRATIVE, BluefinWebPortalConstants.MANAGEALLUSERS)

				// Session
				.antMatchers(HttpMethod.POST, sessionApiBaseURL, sessionApiBaseURL + "/",
						sessionApiBaseURL + "/recovery/password", sessionApiBaseURL + "/recovery/password/")
				.permitAll()
				.antMatchers(HttpMethod.POST, sessionApiBaseURL + "/consumer/{"+BluefinWebPortalConstants.USERNAME+"}",
						sessionApiBaseURL + "/consumer/{"+BluefinWebPortalConstants.USERNAME+"}/")
				.hasAuthority(BluefinWebPortalConstants.ADMINISTRATIVE).antMatchers(HttpMethod.PUT, sessionApiBaseURL, sessionApiBaseURL + "/")
				.authenticated().antMatchers(HttpMethod.DELETE, sessionApiBaseURL, sessionApiBaseURL + "/")
				.authenticated()

				// Users
				.antMatchers(HttpMethod.GET, usersApiBaseURL, usersApiBaseURL + "/")
				.hasAnyAuthority(BluefinWebPortalConstants.ADMINISTRATIVE, BluefinWebPortalConstants.MANAGEALLUSERS)
				.antMatchers(HttpMethod.GET, usersApiBaseURL + "/{"+BluefinWebPortalConstants.USERNAME+"}", usersApiBaseURL + "/{"+BluefinWebPortalConstants.USERNAME+"}/")
				.hasAnyAuthority("MANAGE_CURRENT_USER", BluefinWebPortalConstants.ADMINISTRATIVE, BluefinWebPortalConstants.MANAGEALLUSERS)
				.antMatchers(HttpMethod.POST, usersApiBaseURL, usersApiBaseURL + "/")
				.hasAnyAuthority(BluefinWebPortalConstants.ADMINISTRATIVE, BluefinWebPortalConstants.MANAGEALLUSERS)
				.antMatchers(HttpMethod.PUT, usersApiBaseURL + "/{"+BluefinWebPortalConstants.USERNAME+"}", usersApiBaseURL + "/{"+BluefinWebPortalConstants.USERNAME+"}/",
						usersApiBaseURL + "/{"+BluefinWebPortalConstants.USERNAME+"}/password", usersApiBaseURL + "/{"+BluefinWebPortalConstants.USERNAME+"}/password/")
				.hasAnyAuthority("MANAGE_CURRENT_USER", BluefinWebPortalConstants.ADMINISTRATIVE, BluefinWebPortalConstants.MANAGEALLUSERS)
				.antMatchers(HttpMethod.PUT, usersApiBaseURL + "/{"+BluefinWebPortalConstants.USERNAME+"}/legal-entities",
						usersApiBaseURL + "/{"+BluefinWebPortalConstants.USERNAME+"}/legal-entities/", usersApiBaseURL + "/{"+BluefinWebPortalConstants.USERNAME+"}/roles",
						usersApiBaseURL + "/{"+BluefinWebPortalConstants.USERNAME+"}/roles/")
				.hasAnyAuthority(BluefinWebPortalConstants.ADMINISTRATIVE, BluefinWebPortalConstants.MANAGEALLUSERS)
				.antMatchers(HttpMethod.DELETE, usersApiBaseURL + "/{"+BluefinWebPortalConstants.USERNAME+"}", usersApiBaseURL + "/{"+BluefinWebPortalConstants.USERNAME+"}/")
				.hasAnyAuthority(BluefinWebPortalConstants.ADMINISTRATIVE, BluefinWebPortalConstants.MANAGEALLUSERS)

				// Legal entities
				.antMatchers(HttpMethod.GET, legalEntitiesApiBaseURL, legalEntitiesApiBaseURL + "/",
						legalEntitiesApiBaseURL + "/{"+BluefinWebPortalConstants.ID+"}", legalEntitiesApiBaseURL + "/{"+BluefinWebPortalConstants.ID+"}/")
				.authenticated().antMatchers(HttpMethod.POST, legalEntitiesApiBaseURL, legalEntitiesApiBaseURL + "/")
				.hasAuthority(BluefinWebPortalConstants.PRODUCT_CONFIGURATION)
				.antMatchers(HttpMethod.PUT, legalEntitiesApiBaseURL + "/{"+BluefinWebPortalConstants.ID+"}", legalEntitiesApiBaseURL + "/{"+BluefinWebPortalConstants.ID+"}/")
				.hasAuthority(BluefinWebPortalConstants.PRODUCT_CONFIGURATION)
				.antMatchers(HttpMethod.DELETE, legalEntitiesApiBaseURL + "/{"+BluefinWebPortalConstants.ID+"}", legalEntitiesApiBaseURL + "/{"+BluefinWebPortalConstants.ID+"}/")
				.hasAuthority(BluefinWebPortalConstants.PRODUCT_CONFIGURATION)

				// Reconciliation Status
				.antMatchers(HttpMethod.GET, reconciliationStatusApiBaseURL, reconciliationStatusApiBaseURL + "/",
						reconciliationStatusApiBaseURL + "/{"+BluefinWebPortalConstants.ID+"}", reconciliationStatusApiBaseURL + "/{"+BluefinWebPortalConstants.ID+"}/")
				.authenticated()

				// Payment Processors
				.antMatchers(HttpMethod.GET, paymentProcessorApiBaseURL, paymentProcessorApiBaseURL + "/",
						paymentProcessorApiBaseURL + "/{"+BluefinWebPortalConstants.ID+"}", paymentProcessorApiBaseURL + "/{"+BluefinWebPortalConstants.ID+"}/")
				.authenticated()
				.antMatchers(HttpMethod.POST, paymentProcessorApiBaseURL, paymentProcessorApiBaseURL + "/")
				.hasAuthority(BluefinWebPortalConstants.PRODUCT_CONFIGURATION)
				.antMatchers(HttpMethod.PUT, paymentProcessorApiBaseURL + "/{"+BluefinWebPortalConstants.ID+"}",
						paymentProcessorApiBaseURL + "/{"+BluefinWebPortalConstants.ID+"}/",
						paymentProcessorApiBaseURL + "/{"+BluefinWebPortalConstants.ID+"}/payment-processor-merchants",
						paymentProcessorApiBaseURL + "/{"+BluefinWebPortalConstants.ID+"}/payment-processor-merchants/")
				.hasAuthority(BluefinWebPortalConstants.PRODUCT_CONFIGURATION)
				.antMatchers(HttpMethod.DELETE, paymentProcessorApiBaseURL + "/{"+BluefinWebPortalConstants.ID+"}",
						paymentProcessorApiBaseURL + "/{"+BluefinWebPortalConstants.ID+"}/")
				.hasAuthority(BluefinWebPortalConstants.PRODUCT_CONFIGURATION)

				// Payment Processor Rules
				.antMatchers(HttpMethod.GET, paymentProcessorRulesApiBaseURL, paymentProcessorRulesApiBaseURL + "/",
						paymentProcessorRulesApiBaseURL + "/{"+BluefinWebPortalConstants.ID+"}", paymentProcessorRulesApiBaseURL + "/{"+BluefinWebPortalConstants.ID+"}/",
						paymentProcessorRulesApiBaseURL + "/transaction-types",
						paymentProcessorRulesApiBaseURL + "/transaction-types/")
				.hasAuthority(BluefinWebPortalConstants.PRODUCT_CONFIGURATION)
				.antMatchers(HttpMethod.POST, paymentProcessorRulesApiBaseURL, paymentProcessorRulesApiBaseURL + "/")
				.hasAuthority(BluefinWebPortalConstants.PRODUCT_CONFIGURATION)
				.antMatchers(HttpMethod.PUT, paymentProcessorRulesApiBaseURL + "/{"+BluefinWebPortalConstants.ID+"}",
						paymentProcessorRulesApiBaseURL + "/{"+BluefinWebPortalConstants.ID+"}/")
				.hasAuthority(BluefinWebPortalConstants.PRODUCT_CONFIGURATION)
				.antMatchers(HttpMethod.DELETE, paymentProcessorRulesApiBaseURL + "/{"+BluefinWebPortalConstants.ID+"}",
						paymentProcessorRulesApiBaseURL + "/{"+BluefinWebPortalConstants.ID+"}/")
				.hasAuthority(BluefinWebPortalConstants.PRODUCT_CONFIGURATION)

				// Payment Processor Remittance
				.antMatchers(HttpMethod.GET, paymentProcessorRemittanceApiBaseURL,
						paymentProcessorRemittanceApiBaseURL + "/", paymentProcessorRemittanceApiBaseURL + "/{"+BluefinWebPortalConstants.ID+"}",
						paymentProcessorRemittanceApiBaseURL + "/{"+BluefinWebPortalConstants.ID+"}/")
				.hasAuthority("SEARCH_RECONCILIATION")

				// Roles
				.antMatchers(HttpMethod.GET, rolesApiBaseURL, rolesApiBaseURL + "/").authenticated()
				.antMatchers(HttpMethod.GET, rolesApiBaseURL + "/{"+BluefinWebPortalConstants.ID+"}", rolesApiBaseURL + "/{"+BluefinWebPortalConstants.ID+"}/").authenticated()
				.antMatchers(HttpMethod.PUT, rolesAssignPerApiBaseURL, rolesAssignPerApiBaseURL + "/").hasAnyAuthority(BluefinWebPortalConstants.ADMINISTRATIVE)

				// Internal Response Codes
				 .antMatchers(HttpMethod.GET, internalResponseCodesApiBaseURL, internalResponseCodesApiBaseURL + "/")
				.hasAnyAuthority(BluefinWebPortalConstants.ADMINISTRATIVE, BluefinWebPortalConstants.MANAGERESPONSECODES)
				.antMatchers(HttpMethod.POST, internalResponseCodesApiBaseURL, internalResponseCodesApiBaseURL + "/")
				.hasAnyAuthority(BluefinWebPortalConstants.ADMINISTRATIVE, BluefinWebPortalConstants.MANAGERESPONSECODES)
				.antMatchers(HttpMethod.PUT, internalResponseCodesApiBaseURL, internalResponseCodesApiBaseURL + "/")
				.hasAnyAuthority(BluefinWebPortalConstants.ADMINISTRATIVE, BluefinWebPortalConstants.MANAGERESPONSECODES)
				.antMatchers(HttpMethod.DELETE, internalResponseCodesApiBaseURL + "/{"+BluefinWebPortalConstants.ID+"}",
						internalResponseCodesApiBaseURL + "/{"+BluefinWebPortalConstants.ID+"}/")
				.hasAnyAuthority(BluefinWebPortalConstants.ADMINISTRATIVE, BluefinWebPortalConstants.MANAGERESPONSECODES)


				.antMatchers(HttpMethod.GET, merchantApiBaseURL, merchantApiBaseURL + "/")
				.hasAnyAuthority(BluefinWebPortalConstants.PRODUCT_CONFIGURATION, BluefinWebPortalConstants.MANAGERESPONSECODES)
				.antMatchers(HttpMethod.PUT, merchantApiBaseURL, merchantApiBaseURL + "/")
				.hasAnyAuthority(BluefinWebPortalConstants.PRODUCT_CONFIGURATION, BluefinWebPortalConstants.MANAGERESPONSECODES)

				// Internal Status Codes
				.antMatchers(HttpMethod.GET, internalStatusCodesApiBaseURL, internalStatusCodesApiBaseURL + "/")
				.authenticated()
				.antMatchers(HttpMethod.POST, internalStatusCodesApiBaseURL, internalStatusCodesApiBaseURL + "/")
				.hasAuthority(BluefinWebPortalConstants.ADMINISTRATIVE)
				.antMatchers(HttpMethod.PUT, internalStatusCodesApiBaseURL, internalStatusCodesApiBaseURL + "/")
				.hasAuthority(BluefinWebPortalConstants.ADMINISTRATIVE)
				.antMatchers(HttpMethod.DELETE, internalStatusCodesApiBaseURL + "/{"+BluefinWebPortalConstants.ID+"}",
						internalStatusCodesApiBaseURL + "/{"+BluefinWebPortalConstants.ID+"}/")
				.hasAuthority(BluefinWebPortalConstants.ADMINISTRATIVE)

				// Batch Uploads
				.antMatchers(HttpMethod.GET, batchUploadApiBaseURL, batchUploadApiBaseURL + "/")
				.hasAuthority("BATCH_UPLOAD")
				.antMatchers(HttpMethod.GET, batchUploadApiBaseURL + "/{"+BluefinWebPortalConstants.ID+"}", batchUploadApiBaseURL + "/{"+BluefinWebPortalConstants.ID+"}/")
				.hasAuthority(BluefinWebPortalConstants.BATCHREPORTING)




				// Application Property Lookup
				.antMatchers(HttpMethod.GET, applicationPropertyApiBaseURL)
				.permitAll()
				.antMatchers(HttpMethod.POST, applicationPropertyApiBaseURL)
				.permitAll()
				.antMatchers(HttpMethod.PUT, applicationPropertyApiBaseURL)
				.permitAll()
				.antMatchers(HttpMethod.DELETE, applicationPropertyApiBaseURL)
				.permitAll()
		
				.and().authorizeRequests().anyRequest().authenticated();
		// @formatter:on

        // Custom JWT based authentication
        httpSecurity.addFilterBefore(authenticationTokenFilterBean(), UsernamePasswordAuthenticationFilter.class);
    }

}
