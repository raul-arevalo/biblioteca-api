package com.biblioteca.service;

import com.biblioteca.dto.LibroDTO;
import com.biblioteca.entity.Autor;
import com.biblioteca.entity.Categoria;
import com.biblioteca.entity.Libro;
import com.biblioteca.exception.ResourceNotFoundException;
import com.biblioteca.mapper.LibroMapper;
import com.biblioteca.repository.AutorRepository;
import com.biblioteca.repository.CategoriaRepository;
import com.biblioteca.repository.LibroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class LibroService {

    private final LibroRepository libroRepository;
    private final AutorRepository autorRepository;
    private final CategoriaRepository categoriaRepository;
    private final LibroMapper libroMapper;

    @Transactional(readOnly = true)
    public List<LibroDTO> listar() {
        return libroRepository.findAll()
                .stream()
                .map(libroMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public LibroDTO obtenerPorId(Long id) {
        return libroMapper.toDTO(buscarEntidadPorId(id));
    }

    @Transactional(readOnly = true)
    public List<LibroDTO> buscarPorTitulo(String titulo) {
        return libroRepository.findByTituloContainingIgnoreCase(titulo)
                .stream()
                .map(libroMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LibroDTO> listarDisponibles() {
        return libroRepository.findByDisponibleTrue()
                .stream()
                .map(libroMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LibroDTO> listarPorAutor(Long autorId) {
        return libroRepository.findByAutorId(autorId)
                .stream()
                .map(libroMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LibroDTO> listarPorCategoria(Long categoriaId) {
        return libroRepository.findByCategoriaId(categoriaId)
                .stream()
                .map(libroMapper::toDTO)
                .toList();
    }

    public LibroDTO crear(LibroDTO dto) {
        if (libroRepository.existsByIsbn(dto.getIsbn())) {
            throw new IllegalStateException("Ya existe un libro con el ISBN: " + dto.getIsbn());
        }

        Libro libro = libroMapper.toEntity(dto);
        libro.setId(null);
        libro.setAutor(buscarAutor(dto.getAutorId()));
        libro.setCategoria(buscarCategoria(dto.getCategoriaId()));

        if (libro.getStock() == null) {
            libro.setStock(0);
        }
        libro.setDisponible(libro.getStock() > 0);

        return libroMapper.toDTO(libroRepository.save(libro));
    }

    public LibroDTO actualizar(Long id, LibroDTO dto) {
        Libro libro = buscarEntidadPorId(id);

        boolean isbnCambio = !libro.getIsbn().equals(dto.getIsbn());
        if (isbnCambio && libroRepository.existsByIsbn(dto.getIsbn())) {
            throw new IllegalStateException("Ya existe un libro con el ISBN: " + dto.getIsbn());
        }

        libro.setTitulo(dto.getTitulo());
        libro.setIsbn(dto.getIsbn());
        libro.setAnioPublicacion(dto.getAnioPublicacion());
        libro.setEditorial(dto.getEditorial());
        libro.setNumeroPaginas(dto.getNumeroPaginas());
        libro.setIdioma(dto.getIdioma());
        libro.setStock(dto.getStock());
        libro.setDisponible(dto.getStock() != null && dto.getStock() > 0);
        libro.setAutor(buscarAutor(dto.getAutorId()));
        libro.setCategoria(buscarCategoria(dto.getCategoriaId()));

        return libroMapper.toDTO(libroRepository.save(libro));
    }

    public void eliminar(Long id) {
        Libro libro = buscarEntidadPorId(id);
        libroRepository.delete(libro);
    }

    private Libro buscarEntidadPorId(Long id) {
        return libroRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Libro", id));
    }

    private Autor buscarAutor(Long autorId) {
        return autorRepository.findById(autorId)
                .orElseThrow(() -> new ResourceNotFoundException("Autor", autorId));
    }

    private Categoria buscarCategoria(Long categoriaId) {
        return categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría", categoriaId));
    }
}