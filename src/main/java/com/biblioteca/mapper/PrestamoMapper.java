package com.biblioteca.mapper;

import com.biblioteca.dto.PrestamoDTO;
import com.biblioteca.entity.Prestamo;
import org.springframework.stereotype.Component;

@Component
public class PrestamoMapper {

    public PrestamoDTO toDTO(Prestamo prestamo) {
        if (prestamo == null) {
            return null;
        }
        String nombreCompleto = null;
        if (prestamo.getUsuario() != null) {
            nombreCompleto = prestamo.getUsuario().getNombres() + " " + prestamo.getUsuario().getApellidos();
        }
        return PrestamoDTO.builder()
                .id(prestamo.getId())
                .usuarioId(prestamo.getUsuario() != null ? prestamo.getUsuario().getId() : null)
                .usuarioNombreCompleto(nombreCompleto)
                .libroId(prestamo.getLibro() != null ? prestamo.getLibro().getId() : null)
                .libroTitulo(prestamo.getLibro() != null ? prestamo.getLibro().getTitulo() : null)
                .fechaPrestamo(prestamo.getFechaPrestamo())
                .fechaDevolucion(prestamo.getFechaDevolucion())
                .estado(prestamo.getEstado())
                .observaciones(prestamo.getObservaciones())
                .createdAt(prestamo.getCreatedAt())
                .build();
    }

    /**
     * Mapea únicamente los campos escalares. Las relaciones (usuario, libro)
     * deben ser asignadas por el Service tras buscarlas por su ID.
     */
    public Prestamo toEntity(PrestamoDTO dto) {
        if (dto == null) {
            return null;
        }
        return Prestamo.builder()
                .id(dto.getId())
                .fechaPrestamo(dto.getFechaPrestamo())
                .fechaDevolucion(dto.getFechaDevolucion())
                .estado(dto.getEstado())
                .observaciones(dto.getObservaciones())
                .build();
    }
}