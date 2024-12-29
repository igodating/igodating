package com.bpcbt.marketplace.boot.chat.controller;

import com.bpcbt.marketplace.boot.chat.exception.ChatMemberCreateException;
import com.bpcbt.marketplace.boot.chat.exception.UserNotAuthorizedException;
import com.bpcbt.marketplace.boot.commons.util.ExceptionToStringConverter;
import com.bpcbt.marketplace.boot.commons.web.controller.BaseRestExceptionControllerAdvice;
import com.bpcbt.marketplace.commons.ActionResult;
import com.bpcbt.marketplace.commons.exception.ApiErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestControllerAdvice
@Log4j2
public class ChatServiceExceptionControllerAdvice extends BaseRestExceptionControllerAdvice {

    private final ExceptionToStringConverter exceptionToStringConverter;

    public ChatServiceExceptionControllerAdvice(ExceptionToStringConverter exceptionToStringConverter) {
        this.exceptionToStringConverter = exceptionToStringConverter;
    }

    @ExceptionHandler(Exception.class)
    public ActionResult<Void> handleException(Exception e, ServletWebRequest webRequest) {
        final HttpServletRequest request = webRequest.getRequest();
        log.error("Error occurred while handling api request. URL {}, Method {}, Headers {}, principal {}",
                ServletUriComponentsBuilder.fromRequest(webRequest.getRequest()).toUriString(), request.getMethod(), this.getRequestHeaders(request), this.request.getUserPrincipal(), e);
        return ActionResult.fail(ApiErrorCode.INTERNAL_SERVER_ERROR, this.exceptionToStringConverter.convert(e));
    }

    @ExceptionHandler(ChatMemberCreateException.class)
    public ActionResult<Void> handleChatMemberCreateException(ChatMemberCreateException e) {
        return ActionResult.fail(ApiErrorCode.CHAT_MEMBER_CREATE, e.getMessage());
    }

    @ExceptionHandler(UserNotAuthorizedException.class)
    public ActionResult<Void> handleUserNotAuthorizedException(UserNotAuthorizedException e) {
        final ActionResult<Void> returnValue = ActionResult.fail(ApiErrorCode.UNAUTHORIZED, e.getMessage());
        returnValue.setStatus(HttpStatus.UNAUTHORIZED);
        return returnValue;
    }
}
