package com.mcmcg.ico.bluefin.rest.resource;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ErrorResource implements Serializable {
    private static final long serialVersionUID = -998746769406083432L;

    public static final String REQUEST_HEADER_PROFILE = "profile";
    public static final String REQUEST_HEADER_PROFILE_DEVELOPMENT = "development";

    private UUID uniqueErrorId;
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
     * @param uniqueErrorId
     * @param exception
     * @param hasDevelopmentProfile
     *            if true, it displays additional information related to
     *            development profile; otherwise it won't
     * @return ErrorResource object that holds the exception information
     */
    public static ErrorResource buildErrorResource(UUID uniqueErrorId, final Exception exception,
            final boolean hasDevelopmentProfile) {
        ErrorResource em = new ErrorResource();
        em.setUniqueErrorId(uniqueErrorId);
        em.setTimestamp(Calendar.getInstance().getTimeInMillis());
        em.setMessage(exception.getMessage());
        em.setException(exception.getClass().getName());

        // Enable additional information when development profile is on
        if (hasDevelopmentProfile) {
            StringWriter sw = new StringWriter();
            exception.printStackTrace(new PrintWriter(sw));
            em.setTrace(sw.toString());
        }

        return em;
    }
}
