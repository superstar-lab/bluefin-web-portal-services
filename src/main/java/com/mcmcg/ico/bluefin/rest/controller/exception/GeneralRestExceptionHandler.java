package com.mcmcg.ico.bluefin.rest.controller.exception;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.mcmcg.ico.bluefin.rest.resource.ErrorResource;

@ControllerAdvice
public class GeneralRestExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeneralRestExceptionHandler.class);

    @ExceptionHandler(CustomNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public @ResponseBody ErrorResource handleNotFoundException(final Exception exception, WebRequest request) {
        UUID uniqueErrorId = logException(exception);

        return ErrorResource.buildErrorResource(uniqueErrorId, exception, hasDevelopmentProfileHeader(request));
    }

    @ExceptionHandler(CustomForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public @ResponseBody ErrorResource handleForbiddenException(final Exception exception, WebRequest request) {
        UUID uniqueErrorId = logException(exception);

        return ErrorResource.buildErrorResource(uniqueErrorId, exception, hasDevelopmentProfileHeader(request));
    }

    @ExceptionHandler({ CustomException.class, Exception.class })
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public @ResponseBody ErrorResource handleGeneralException(final Exception exception, WebRequest request) {
        UUID uniqueErrorId = logException(exception);

        return ErrorResource.buildErrorResource(uniqueErrorId, exception, hasDevelopmentProfileHeader(request));
    }

    @ExceptionHandler({ AuthenticationException.class, CustomUnauthorizedException.class })
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public @ResponseBody ErrorResource handleUnauthorizedException(final Exception exception, WebRequest request) {
        UUID uniqueErrorId = logException(exception);

        return ErrorResource.buildErrorResource(uniqueErrorId, exception, hasDevelopmentProfileHeader(request));
    }

    @ExceptionHandler({ CustomBadRequestException.class, IllegalArgumentException.class })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResource handleBadRequestException(final Exception exception, WebRequest request) {
        UUID uniqueErrorId = logException(exception);

        return ErrorResource.buildErrorResource(uniqueErrorId, exception, hasDevelopmentProfileHeader(request));
    }

    private UUID logException(final Exception exception) {
        final UUID uniqueErrorId = UUID.randomUUID();
        LOGGER.error("[id={}] Error on rest call", uniqueErrorId, exception);

        return uniqueErrorId;
    }

    private boolean hasDevelopmentProfileHeader(WebRequest request) {
        final String profile = request.getHeader("profile");

        return profile == null ? false : profile.equalsIgnoreCase("development");
    }
}
