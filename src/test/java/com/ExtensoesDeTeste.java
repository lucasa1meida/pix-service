package com;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExtensoesDeTeste {
    public static <T extends Throwable> void comMensagemDeErro(T exception, String message) {
        assertEquals(exception.getMessage(), message);
    }
}
