package com.mcmcg.ico.bluefin.rest.resource;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Calendar;
import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ErrorResource implements Serializable {
    private static final long serialVersionUID = -998746769406083432L;
    public static final String REQUEST_HEADER_PROFILE = "profile";
    public static final String REQUEST_HEADER_PROFILE_DEVELOPMENT = "development";

    private UUID uniqueId;
    private long timestamp;
    private String message;
    private String exception;
    private String trace;

    /**
     * Build error resource when an exception happen without adding the trace
     * property (stacktrace)
     * 
     * @param uniqueErrorId
     * @param exception
     * @return ErrorResource object that holds the exception information
     */
    public static ErrorResource buildErrorResource(UUID uniqueErrorId, final Exception exception) {
        return buildErrorResource(uniqueErrorId, exception, false);
    }

    /**
     * Build error resource when an exception happen
     * 
     * @param uniqueId
     *            unique uuid to map external error with logs
     * @param ex
     *            class exceptionn
     * @param hasDevelopmentProfile
     *            if true, it displays additional information related to
     *            development profile; otherwise it won't
     * @return ErrorResource object that holds the exception information
     */
    public static ErrorResource buildErrorResource(UUID uniqueId, final Exception exp,
            final boolean hasDevelopmentProfile) {
        ErrorResource em = new ErrorResource();
        em.setUniqueId(uniqueId);
        em.setTimestamp(Calendar.getInstance().getTimeInMillis());
        em.setMessage(exp.getMessage());
        em.setException(exp.getClass().getName());
        
        // Enable additional information when development profile is on
        if (hasDevelopmentProfile) {
        	/** After discussion with Matloob, we need to comment below code due to sonar qube scan giving issue 
        	 * and use below line
        	 *	StringWriter sw = new StringWriter();
             *	exception.printStackTrace(new PrintWriter(sw));
             *	em.setTrace(sw.toString());
            */
        	em.setTrace(Arrays.toString(exp.getStackTrace())); 
        }

        return em;
    }

    /**
     * Build a response entity using the application error resource format
     * 
     * @param httpStatus
     *            http status code
     * @param uniqueId
     *            unique uuid to map external error with logs
     * @param ex
     *            class exception
     * @param hasDevelopmentProfileHeader
     *            if true, it displays additional information related to
     *            development profile; otherwise it won't
     * @return
     */
    public static ResponseEntity<Object> buildErrorResource(HttpStatus httpStatus, UUID uniqueId, Exception exception,
            boolean hasDevelopmentProfileHeader) {
        return new ResponseEntity<>(
                ErrorResource.buildErrorResource(uniqueId, exception, hasDevelopmentProfileHeader), new HttpHeaders(),
                httpStatus);
    }
}
