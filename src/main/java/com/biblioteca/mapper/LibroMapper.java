package com.biblioteca.mapper;

import com.biblioteca.dto.LibroDTO;
import com.biblioteca.entity.Libro;
import org.springframework.stereotype.Component;

@Component
public class LibroMapper {

    public LibroDTO toDTO(Libro libro) {
        if (libro == null) {
            return null;
        }
        return LibroDTO.builder()
                .id(libro.getId())
                .titulo(libro.getTitulo())
                .isbn(libro.getIsbn())
                .anioPublicacion(libro.getAnioPublicacion())
                .editorial(libro.getEditorial())
                .numeroPaginas(libro.getNumeroPaginas())
                .idioma(libro.getIdioma())
                .stock(libro.getStock())
                .disponible(libro.getDisponible())
                .autorId(libro.getAutor() != null ? libro.getAutor().getId() : null)
                .autorNombre(libro.getAutor() != null ? libro.getAutor().getNombre() : null)
                .categoriaId(libro.getCategoria() != null ? libro.getCategoria().getId() : null)
                .categoriaNombre(libro.getCategoria() != null ? libro.getCategoria().getNombre() : null)
                .createdAt(libro.getCreatedAt())
                .updatedAt(libro.getUpdatedAt())
                .build();
    }

    /**
     * Mapea únicamente los campos escalares. Las relaciones (autor, categoria)
     * deben ser asignadas por el Service tras buscarlas por su ID, ya que este
     * mapper no tiene acceso a los repositorios.
     */
    public Libro toEntity(LibroDTO dto) {
        if (dto == null) {
            return null;
        }
        return Libro.builder()
                .id(dto.getId())
                .titulo(dto.getTitulo())
                .isbn(dto.getIsbn())
                .anioPublicacion(dto.getAnioPublicacion())
                .editorial(dto.getEditorial())
                .numeroPaginas(dto.getNumeroPaginas())
                .idioma(dto.getIdioma())
                .stock(dto.getStock())
                .disponible(dto.getDisponible())
                .build();
    }
}