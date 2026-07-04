package com.biblioteca.repository;

import com.biblioteca.entity.Prestamo;
import com.biblioteca.enums.EstadoPrestamo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {

    List<Prestamo> findByUsuarioId(Long usuarioId);

    List<Prestamo> findByLibroId(Long libroId);

    List<Prestamo> findByEstado(EstadoPrestamo estado);

    List<Prestamo> findByUsuarioIdAndEstado(Long usuarioId, EstadoPrestamo estado);

    List<Prestamo> findByLibroIdAndEstado(Long libroId, EstadoPrestamo estado);

    long countByLibroIdAndEstado(Long libroId, EstadoPrestamo estado);
}