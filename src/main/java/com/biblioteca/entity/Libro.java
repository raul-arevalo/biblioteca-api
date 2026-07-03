package com.biblioteca.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa un libro del catálogo.
 * Mapea la tabla "libros" definida en el script SQL.
 *
 * Nota: "editorial" se maneja como campo de texto simple (VARCHAR), no como
 * entidad/tabla independiente, tal como está definido en el script SQL.
 */
@Entity
@Table(name = "libros")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"autor", "categoria", "prestamos"})
public class Libro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "titulo", nullable = false, length = 200)
    private String titulo;

    @Column(name = "isbn", nullable = false, unique = true, length = 20)
    private String isbn;

    // SMALLINT en SQL -> Short en Java
    @Column(name = "anio_publicacion")
    private Short anioPublicacion;

    @Column(name = "editorial", length = 120)
    private String editorial;

    @Column(name = "numero_paginas")
    private Integer numeroPaginas;

    @Column(name = "idioma", length = 10)
    private String idioma;

    @Builder.Default
    @Column(name = "stock", nullable = false)
    private Integer stock = 0;

    @Builder.Default
    @Column(name = "disponible")
    private Boolean disponible = true;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "autor_id", nullable = false, foreignKey = @ForeignKey(name = "fk_libro_autor"))
    private Autor autor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false, foreignKey = @ForeignKey(name = "fk_libro_categoria"))
    private Categoria categoria;

    // Gestionados por la base de datos (DEFAULT + trigger set_timestamp_libros)
    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private OffsetDateTime updatedAt;

    // Sin cascade de borrado: la FK tiene ON DELETE RESTRICT.
    @Builder.Default
    @OneToMany(mappedBy = "libro", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    private List<Prestamo> prestamos = new ArrayList<>();
}