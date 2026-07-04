package com.biblioteca.service;

import com.biblioteca.dto.AutorDTO;
import com.biblioteca.entity.Autor;
import com.biblioteca.exception.ResourceNotFoundException;
import com.biblioteca.mapper.AutorMapper;
import com.biblioteca.repository.AutorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AutorService {

    private final AutorRepository autorRepository;
    private final AutorMapper autorMapper;

    @Transactional(readOnly = true)
    public List<AutorDTO> listar() {
        return autorRepository.findAll()
                .stream()
                .map(autorMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public AutorDTO obtenerPorId(Long id) {
        Autor autor = buscarEntidadPorId(id);
        return autorMapper.toDTO(autor);
    }

    @Transactional(readOnly = true)
    public List<AutorDTO> buscarPorNombre(String nombre) {
        return autorRepository.findByNombreContainingIgnoreCase(nombre)
                .stream()
                .map(autorMapper::toDTO)
                .toList();
    }

    public AutorDTO crear(AutorDTO dto) {
        Autor autor = autorMapper.toEntity(dto);
        autor.setId(null);
        Autor guardado = autorRepository.save(autor);
        return autorMapper.toDTO(guardado);
    }

    public AutorDTO actualizar(Long id, AutorDTO dto) {
        Autor autor = buscarEntidadPorId(id);
        autor.setNombre(dto.getNombre());
        autor.setNacionalidad(dto.getNacionalidad());
        autor.setFechaNacimiento(dto.getFechaNacimiento());
        autor.setBiografia(dto.getBiografia());
        return autorMapper.toDTO(autorRepository.save(autor));
    }

    public void eliminar(Long id) {
        Autor autor = buscarEntidadPorId(id);
        autorRepository.delete(autor);
    }

    private Autor buscarEntidadPorId(Long id) {
        return autorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Autor", id));
    }
}