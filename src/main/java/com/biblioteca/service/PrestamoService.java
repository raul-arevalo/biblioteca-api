package com.biblioteca.service;

import com.biblioteca.dto.PrestamoDTO;
import com.biblioteca.entity.Libro;
import com.biblioteca.entity.Prestamo;
import com.biblioteca.entity.Usuario;
import com.biblioteca.enums.EstadoPrestamo;
import com.biblioteca.exception.ResourceNotFoundException;
import com.biblioteca.mapper.PrestamoMapper;
import com.biblioteca.repository.LibroRepository;
import com.biblioteca.repository.PrestamoRepository;
import com.biblioteca.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PrestamoService {

    private final PrestamoRepository prestamoRepository;
    private final UsuarioRepository usuarioRepository;
    private final LibroRepository libroRepository;
    private final PrestamoMapper prestamoMapper;

    @Transactional(readOnly = true)
    public List<PrestamoDTO> listar() {
        return prestamoRepository.findAll()
                .stream()
                .map(prestamoMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public PrestamoDTO obtenerPorId(Long id) {
        return prestamoMapper.toDTO(buscarEntidadPorId(id));
    }

    @Transactional(readOnly = true)
    public List<PrestamoDTO> listarPorUsuario(Long usuarioId) {
        return prestamoRepository.findByUsuarioId(usuarioId)
                .stream()
                .map(prestamoMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PrestamoDTO> listarPorLibro(Long libroId) {
        return prestamoRepository.findByLibroId(libroId)
                .stream()
                .map(prestamoMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PrestamoDTO> listarPorEstado(EstadoPrestamo estado) {
        return prestamoRepository.findByEstado(estado)
                .stream()
                .map(prestamoMapper::toDTO)
                .toList();
    }

    public PrestamoDTO crear(PrestamoDTO dto) {
        Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", dto.getUsuarioId()));

        if (Boolean.FALSE.equals(usuario.getActivo())) {
            throw new IllegalStateException("El usuario está inactivo y no puede tomar préstamos.");
        }

        Libro libro = libroRepository.findById(dto.getLibroId())
                .orElseThrow(() -> new ResourceNotFoundException("Libro", dto.getLibroId()));

        if (libro.getStock() == null || libro.getStock() <= 0) {
            throw new IllegalStateException("No hay stock disponible para el libro: " + libro.getTitulo());
        }

        libro.setStock(libro.getStock() - 1);
        libro.setDisponible(libro.getStock() > 0);
        libroRepository.save(libro);

        Prestamo prestamo = Prestamo.builder()
                .usuario(usuario)
                .libro(libro)
                .fechaPrestamo(dto.getFechaPrestamo() != null ? dto.getFechaPrestamo() : LocalDate.now())
                .estado(EstadoPrestamo.PRESTADO)
                .observaciones(dto.getObservaciones())
                .build();

        return prestamoMapper.toDTO(prestamoRepository.save(prestamo));
    }

    public PrestamoDTO devolver(Long id) {
        Prestamo prestamo = buscarEntidadPorId(id);

        if (prestamo.getEstado() == EstadoPrestamo.DEVUELTO) {
            throw new IllegalStateException("El préstamo ya fue devuelto.");
        }

        prestamo.setEstado(EstadoPrestamo.DEVUELTO);
        prestamo.setFechaDevolucion(LocalDate.now());

        Libro libro = prestamo.getLibro();
        libro.setStock(libro.getStock() + 1);
        libro.setDisponible(true);
        libroRepository.save(libro);

        return prestamoMapper.toDTO(prestamoRepository.save(prestamo));
    }

    public void eliminar(Long id) {
        Prestamo prestamo = buscarEntidadPorId(id);
        prestamoRepository.delete(prestamo);
    }

    private Prestamo buscarEntidadPorId(Long id) {
        return prestamoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Préstamo", id));
    }
}