package com.bruno.sistemabancario.domain.utils;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CustomMessageResolver {

    private final MessageSource messageSource;

    public CustomMessageResolver(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String getMessage(Code code) {
        return messageSource.getMessage(code.getMessageKey(), null, LocaleContextHolder.getLocale());
    }

    public String getMessage(Code code, Object... args) {
        return messageSource.getMessage(code.getMessageKey(), args, LocaleContextHolder.getLocale());
    }
}
