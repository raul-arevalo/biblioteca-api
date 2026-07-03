package com.biblioteca.entity;

import com.biblioteca.enums.EstadoPrestamo;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * Entidad que representa el préstamo de un libro a un usuario.
 * Mapea la tabla "prestamos" definida en el script SQL.
 */
@Entity
@Table(name = "prestamos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"usuario", "libro"})
public class Prestamo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false, foreignKey = @ForeignKey(name = "fk_prestamo_usuario"))
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "libro_id", nullable = false, foreignKey = @ForeignKey(name = "fk_prestamo_libro"))
    private Libro libro;

    // DEFAULT CURRENT_DATE en la BD, pero se permite fijar el valor desde la app
    @Builder.Default
    @Column(name = "fecha_prestamo", nullable = false)
    private LocalDate fechaPrestamo = LocalDate.now();

    @Column(name = "fecha_devolucion")
    private LocalDate fechaDevolucion;

    // Mapeo al tipo ENUM nativo de PostgreSQL "estado_prestamo" (soporte
    // nativo de Hibernate 6 vía @JdbcTypeCode(SqlTypes.NAMED_ENUM)).
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "estado", nullable = false)
    private EstadoPrestamo estado = EstadoPrestamo.PRESTADO;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    // Gestionado por la base de datos (DEFAULT CURRENT_TIMESTAMP)
    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;
}