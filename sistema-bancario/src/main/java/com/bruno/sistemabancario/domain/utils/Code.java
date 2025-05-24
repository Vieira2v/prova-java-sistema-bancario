package com.bruno.sistemabancario.domain.utils;


import lombok.Getter;

@Getter
public enum Code {

    ACCOUNT_NOT_FOUND("error.account.not_found"),
    NUMBER_ACCOUNT_NOT_FOUND("error.number.account.not_found"),
    TRANSACTION_NOT_FOUND("error.transaction.not_found"),
    TRANSACTION_NOT_APPROVED("error.reversed.transaction.not.approved"),
    INSUFFICIENT_BALANCE("error.account.insufficient_balance"),
    DESTINATION_ACCOUNT_INSUFFICIENT_BALANCE("error.acount.destination.balance"),
    NO_ACCOUNT_FOR_ID("error.no.account.found.this.id"),
    USERNAME_OR_PASSWORD_INCORRECT("error.username.or.password.incorrect"),
    INVALID_USERNAME_OR_PASSWORD("error.invalid.username.or.password"),
    INVALID_TRANSACTION_VALUE("invalid.transaction.value"),
    INVALID_ACCOUNT_NUMBER("invalid.account.number"),
    SOURCE_AND_DESTINATION_SAME("source.and.destination.same"),

    TRANSACTION_APPROVED_SUCCESS("approved.successfully.transaction"),
    TRANSACTION_REVERSED_SUCCESS("reversed.successfully.transaction"),
    USER_REGISTERED_SUCCESS("user.registred.successfully");

    private final String messageKey;

    Code(String messageKey) {
        this.messageKey = messageKey;
    }

}
