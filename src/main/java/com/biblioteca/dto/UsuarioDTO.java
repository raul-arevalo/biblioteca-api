package com.biblioteca.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDTO {

    private Long id;
    private String nombres;
    private String apellidos;
    private String correo;
    private String telefono;
    private String direccion;
    private OffsetDateTime fechaRegistro;
    private Boolean activo;
}