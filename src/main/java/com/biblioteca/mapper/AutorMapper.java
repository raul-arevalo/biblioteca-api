package com.biblioteca.mapper;

import com.biblioteca.dto.AutorDTO;
import com.biblioteca.entity.Autor;
import org.springframework.stereotype.Component;

@Component
public class AutorMapper {

    public AutorDTO toDTO(Autor autor) {
        if (autor == null) {
            return null;
        }
        return AutorDTO.builder()
                .id(autor.getId())
                .nombre(autor.getNombre())
                .nacionalidad(autor.getNacionalidad())
                .fechaNacimiento(autor.getFechaNacimiento())
                .biografia(autor.getBiografia())
                .createdAt(autor.getCreatedAt())
                .updatedAt(autor.getUpdatedAt())
                .build();
    }

    public Autor toEntity(AutorDTO dto) {
        if (dto == null) {
            return null;
        }
        return Autor.builder()
                .id(dto.getId())
                .nombre(dto.getNombre())
                .nacionalidad(dto.getNacionalidad())
                .fechaNacimiento(dto.getFechaNacimiento())
                .biografia(dto.getBiografia())
                .build();
    }
}