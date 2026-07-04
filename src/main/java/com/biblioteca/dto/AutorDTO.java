package com.biblioteca.dto;

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
public class AutorDTO {

    private Long id;
    private String nombre;
    private String nacionalidad;
    private LocalDate fechaNacimiento;
    private String biografia;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}