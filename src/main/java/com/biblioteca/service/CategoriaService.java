package com.biblioteca.service;

import com.biblioteca.dto.CategoriaDTO;
import com.biblioteca.entity.Categoria;
import com.biblioteca.exception.ResourceNotFoundException;
import com.biblioteca.mapper.CategoriaMapper;
import com.biblioteca.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final CategoriaMapper categoriaMapper;

    @Transactional(readOnly = true)
    public List<CategoriaDTO> listar() {
        return categoriaRepository.findAll()
                .stream()
                .map(categoriaMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoriaDTO obtenerPorId(Long id) {
        return categoriaMapper.toDTO(buscarEntidadPorId(id));
    }

    public CategoriaDTO crear(CategoriaDTO dto) {
        if (categoriaRepository.existsByNombreIgnoreCase(dto.getNombre())) {
            throw new IllegalStateException("Ya existe una categoría con el nombre: " + dto.getNombre());
        }
        Categoria categoria = categoriaMapper.toEntity(dto);
        categoria.setId(null);
        return categoriaMapper.toDTO(categoriaRepository.save(categoria));
    }

    public CategoriaDTO actualizar(Long id, CategoriaDTO dto) {
        Categoria categoria = buscarEntidadPorId(id);

        boolean nombreCambio = !categoria.getNombre().equalsIgnoreCase(dto.getNombre());
        if (nombreCambio && categoriaRepository.existsByNombreIgnoreCase(dto.getNombre())) {
            throw new IllegalStateException("Ya existe una categoría con el nombre: " + dto.getNombre());
        }

        categoria.setNombre(dto.getNombre());
        categoria.setDescripcion(dto.getDescripcion());
        return categoriaMapper.toDTO(categoriaRepository.save(categoria));
    }

    public void eliminar(Long id) {
        Categoria categoria = buscarEntidadPorId(id);
        categoriaRepository.delete(categoria);
    }

    private Categoria buscarEntidadPorId(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría", id));
    }
}