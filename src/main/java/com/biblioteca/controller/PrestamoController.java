package com.biblioteca.controller;

import com.biblioteca.dto.PrestamoDTO;
import com.biblioteca.enums.EstadoPrestamo;
import com.biblioteca.service.PrestamoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prestamos")
@RequiredArgsConstructor
public class PrestamoController {

    private final PrestamoService prestamoService;

    @GetMapping
    public ResponseEntity<List<PrestamoDTO>> listar(
            @RequestParam(required = false) Long usuarioId,
            @RequestParam(required = false) Long libroId,
            @RequestParam(required = false) EstadoPrestamo estado) {

        if (usuarioId != null) {
            return ResponseEntity.ok(prestamoService.listarPorUsuario(usuarioId));
        }
        if (libroId != null) {
            return ResponseEntity.ok(prestamoService.listarPorLibro(libroId));
        }
        if (estado != null) {
            return ResponseEntity.ok(prestamoService.listarPorEstado(estado));
        }
        return ResponseEntity.ok(prestamoService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PrestamoDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(prestamoService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<PrestamoDTO> crear(@RequestBody PrestamoDTO dto) {
        PrestamoDTO creado = prestamoService.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PatchMapping("/{id}/devolver")
    public ResponseEntity<PrestamoDTO> devolver(@PathVariable Long id) {
        return ResponseEntity.ok(prestamoService.devolver(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        prestamoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}