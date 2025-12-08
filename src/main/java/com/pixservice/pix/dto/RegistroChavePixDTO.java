package com.pixservice.pix.dto;

import com.pixservice.pix.enums.TipoChavePix;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegistroChavePixDTO {
    private TipoChavePix tipoChavePix;
    private String valorChave;
}