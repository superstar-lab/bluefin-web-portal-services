package com.mcmcg.ico.bluefin.rest.controller.exception;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.util.WebUtils;

import com.mcmcg.ico.bluefin.rest.resource.ErrorResource;

@ControllerAdvice
public class GeneralRestExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GeneralRestExceptionHandler.class);
    private static final String DEVELOPMENT_PROFILE = "development";
    private static final String CUSTOM_HEADER_PROFILE = "profile";

    @ExceptionHandler(CustomNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody public ErrorResource handleNotFoundException(final Exception exception, WebRequest request) {
        UUID uniqueErrorId = logException(exception);

        return ErrorResource.buildErrorResource(uniqueErrorId, exception, hasDevelopmentProfileHeader(request));
    }

    /**
     * JDBCConnectionException.class - Associated with hibernate. Now need to put alternative class which uses in case of JDBC connection failure
     * @param exception
     * @param request
     * @return
     */
    @ExceptionHandler({ DataAccessResourceFailureException.class,
    	
            AccessDeniedException.class })
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ResponseBody public ErrorResource handleForbiddenException(final Exception exception, WebRequest request) {
        UUID uniqueErrorId = logException(exception);

        return ErrorResource.buildErrorResource(uniqueErrorId, exception, hasDevelopmentProfileHeader(request));
    }

    @ExceptionHandler({ CustomException.class, Exception.class })
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody public ErrorResource handleGeneralException(final Exception exception, WebRequest request) {
        UUID uniqueErrorId = logException(exception);

        return ErrorResource.buildErrorResource(uniqueErrorId, exception, hasDevelopmentProfileHeader(request));
    }

    @ExceptionHandler({ IllegalArgumentException.class, CustomBadRequestException.class })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody public ErrorResource handleBadRequestException(final Exception exception, WebRequest request) {
        UUID uniqueErrorId = logException(exception);

        return ErrorResource.buildErrorResource(uniqueErrorId, exception, hasDevelopmentProfileHeader(request));
    }

    @ExceptionHandler({ CustomUnauthorizedException.class })
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ResponseBody public ErrorResource handleUnauthorizedException(final Exception exception, WebRequest request) {
        UUID uniqueErrorId = logException(exception);

        return ErrorResource.buildErrorResource(uniqueErrorId, exception, hasDevelopmentProfileHeader(request));
    }

    /**
     * Customizing exception internal
     */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
            HttpStatus status, WebRequest request) {

        if (HttpStatus.INTERNAL_SERVER_ERROR.equals(status)) {
            request.setAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE, ex, WebRequest.SCOPE_REQUEST);
        }

        return ErrorResource.buildErrorResource(status, logException(ex), ex, hasDevelopmentProfileHeader(request));
    }

    private UUID logException(final Exception exception) {
        final UUID uniqueErrorId = UUID.randomUUID();
        LOG.error("[id={}] Error on rest call", uniqueErrorId, exception);
        return uniqueErrorId;
    }

    private boolean hasDevelopmentProfileHeader(final WebRequest request) {
        final String profile = request.getHeader(CUSTOM_HEADER_PROFILE);

        return profile == null ? false : profile.equalsIgnoreCase(DEVELOPMENT_PROFILE);
    }
}
