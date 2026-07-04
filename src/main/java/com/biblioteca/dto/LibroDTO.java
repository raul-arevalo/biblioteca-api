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
public class LibroDTO {

    private Long id;
    private String titulo;
    private String isbn;
    private Short anioPublicacion;
    private String editorial;
    private Integer numeroPaginas;
    private String idioma;
    private Integer stock;
    private Boolean disponible;

    private Long autorId;
    private String autorNombre;

    private Long categoriaId;
    private String categoriaNombre;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}