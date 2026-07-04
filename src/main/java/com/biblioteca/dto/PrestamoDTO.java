package com.biblioteca.dto;

import com.biblioteca.enums.EstadoPrestamo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrestamoDTO {

    private Long id;

    private Long usuarioId;
    private String usuarioNombreCompleto;

    private Long libroId;
    private String libroTitulo;

    private LocalDate fechaPrestamo;
    private LocalDate fechaDevolucion;
    private EstadoPrestamo estado;
    private String observaciones;

    private OffsetDateTime createdAt;
}