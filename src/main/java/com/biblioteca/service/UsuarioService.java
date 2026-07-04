package com.biblioteca.service;

import com.biblioteca.dto.UsuarioDTO;
import com.biblioteca.entity.Usuario;
import com.biblioteca.exception.ResourceNotFoundException;
import com.biblioteca.mapper.UsuarioMapper;
import com.biblioteca.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;

    @Transactional(readOnly = true)
    public List<UsuarioDTO> listar() {
        return usuarioRepository.findAll()
                .stream()
                .map(usuarioMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public UsuarioDTO obtenerPorId(Long id) {
        return usuarioMapper.toDTO(buscarEntidadPorId(id));
    }

    @Transactional(readOnly = true)
    public List<UsuarioDTO> listarActivos() {
        return usuarioRepository.findByActivoTrue()
                .stream()
                .map(usuarioMapper::toDTO)
                .toList();
    }

    public UsuarioDTO crear(UsuarioDTO dto) {
        if (usuarioRepository.existsByCorreo(dto.getCorreo())) {
            throw new IllegalStateException("Ya existe un usuario con el correo: " + dto.getCorreo());
        }
        Usuario usuario = usuarioMapper.toEntity(dto);
        usuario.setId(null);
        if (usuario.getActivo() == null) {
            usuario.setActivo(true);
        }
        return usuarioMapper.toDTO(usuarioRepository.save(usuario));
    }

    public UsuarioDTO actualizar(Long id, UsuarioDTO dto) {
        Usuario usuario = buscarEntidadPorId(id);

        boolean correoCambio = !usuario.getCorreo().equalsIgnoreCase(dto.getCorreo());
        if (correoCambio && usuarioRepository.existsByCorreo(dto.getCorreo())) {
            throw new IllegalStateException("Ya existe un usuario con el correo: " + dto.getCorreo());
        }

        usuario.setNombres(dto.getNombres());
        usuario.setApellidos(dto.getApellidos());
        usuario.setCorreo(dto.getCorreo());
        usuario.setTelefono(dto.getTelefono());
        usuario.setDireccion(dto.getDireccion());
        if (dto.getActivo() != null) {
            usuario.setActivo(dto.getActivo());
        }
        return usuarioMapper.toDTO(usuarioRepository.save(usuario));
    }

    public void eliminar(Long id) {
        Usuario usuario = buscarEntidadPorId(id);
        usuarioRepository.delete(usuario);
    }

    private Usuario buscarEntidadPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));
    }
}