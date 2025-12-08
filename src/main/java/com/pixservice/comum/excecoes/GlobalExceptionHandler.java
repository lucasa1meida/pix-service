package com.pixservice.comum.excecoes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String MESSAGE = "message";

    @ExceptionHandler(ExcecaoDeDominio.class)
    public ResponseEntity<Map<String, Object>> handleExcecaoDeDominio(ExcecaoDeDominio ex) {
        log.error("Erro: {}", ex.getMessage());
        Map<String, Object> body = new HashMap<>();
        body.put(MESSAGE, ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(NotFoundException ex) {
        log.error("Dados não encontrados: {}", ex.getMessage());
        Map<String, Object> body = new HashMap<>();
        body.put(MESSAGE, ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(InternalServerErrorException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(InternalServerErrorException ex) {
        log.error("Erro Interno: {}", ex.getMessage());
        Map<String, Object> body = new HashMap<>();
        body.put(MESSAGE, ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}