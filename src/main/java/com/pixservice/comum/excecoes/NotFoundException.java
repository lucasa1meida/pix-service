package com.pixservice.comum.excecoes;

public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}