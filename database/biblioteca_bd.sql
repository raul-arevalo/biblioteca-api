-- =========================================================================
-- 1. TIPOS Y FUNCIONES GLOBALES
-- =========================================================================

DO $$ BEGIN
    CREATE TYPE estado_prestamo AS ENUM ('PRESTADO', 'DEVUELTO', 'ATRASADO');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

CREATE OR REPLACE FUNCTION trigger_set_timestamp()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = NOW();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;


-- =========================================================================
-- 2. TABLAS
-- =========================================================================

CREATE TABLE IF NOT EXISTS autores (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(120) NOT NULL,
    nacionalidad VARCHAR(80),
    fecha_nacimiento DATE,
    biografia TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TRIGGER set_timestamp_autores
BEFORE UPDATE ON autores
FOR EACH ROW EXECUTE FUNCTION trigger_set_timestamp();


CREATE TABLE IF NOT EXISTS categorias (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(80) NOT NULL UNIQUE,
    descripcion TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TRIGGER set_timestamp_categorias
BEFORE UPDATE ON categorias
FOR EACH ROW EXECUTE FUNCTION trigger_set_timestamp();


CREATE TABLE IF NOT EXISTS libros (
    id BIGSERIAL PRIMARY KEY,
    titulo VARCHAR(200) NOT NULL,
    isbn VARCHAR(20) NOT NULL UNIQUE,
    anio_publicacion SMALLINT CHECK (anio_publicacion > 0),
    editorial VARCHAR(120),
    numero_paginas INT CHECK (numero_paginas > 0),
    idioma VARCHAR(10),
    stock INT NOT NULL DEFAULT 0 CHECK (stock >= 0),
    disponible BOOLEAN DEFAULT TRUE,
    autor_id BIGINT NOT NULL,
    categoria_id BIGINT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_libro_autor FOREIGN KEY (autor_id) REFERENCES autores(id) ON DELETE RESTRICT,
    CONSTRAINT fk_libro_categoria FOREIGN KEY (categoria_id) REFERENCES categorias(id) ON DELETE RESTRICT
);

CREATE TRIGGER set_timestamp_libros
BEFORE UPDATE ON libros
FOR EACH ROW EXECUTE FUNCTION trigger_set_timestamp();


CREATE TABLE IF NOT EXISTS usuarios (
    id BIGSERIAL PRIMARY KEY,
    nombres VARCHAR(120) NOT NULL,
    apellidos VARCHAR(120) NOT NULL,
    correo VARCHAR(120) NOT NULL UNIQUE,
    telefono VARCHAR(20),
    direccion TEXT,
    fecha_registro TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    activo BOOLEAN DEFAULT TRUE
);


CREATE TABLE IF NOT EXISTS prestamos (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    libro_id BIGINT NOT NULL,
    fecha_prestamo DATE NOT NULL DEFAULT CURRENT_DATE,
    fecha_devolucion DATE,
    estado estado_prestamo NOT NULL DEFAULT 'PRESTADO',
    observaciones TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_prestamo_usuario FOREIGN KEY(usuario_id) REFERENCES usuarios(id) ON DELETE RESTRICT,
    CONSTRAINT fk_prestamo_libro FOREIGN KEY(libro_id) REFERENCES libros(id) ON DELETE RESTRICT,
    CONSTRAINT chk_fechas_prestamo CHECK (fecha_devolucion IS NULL OR fecha_devolucion >= fecha_prestamo)
);


-- =========================================================================
-- 3. ÍNDICES
-- =========================================================================

CREATE INDEX IF NOT EXISTS idx_libros_autor_id ON libros(autor_id);
CREATE INDEX IF NOT EXISTS idx_libros_categoria_id ON libros(categoria_id);
CREATE INDEX IF NOT EXISTS idx_prestamos_usuario_id ON prestamos(usuario_id);
CREATE INDEX IF NOT EXISTS idx_prestamos_libro_id ON prestamos(libro_id);
CREATE INDEX IF NOT EXISTS idx_libros_titulo ON libros(titulo);

CREATE INDEX IF NOT EXISTS idx_prestamos_activos
ON prestamos(estado)
WHERE estado IN ('PRESTADO', 'ATRASADO');


-- =========================================================================
-- 4. DATOS INICIALES
-- =========================================================================

INSERT INTO autores (nombre, nacionalidad, fecha_nacimiento, biografia) VALUES
('Gabriel García Márquez', 'Colombiana', TO_DATE('06-03-1927', 'DD/MM/YYYY'), 'Realismo mágico'),
('George Orwell', 'Británica', TO_DATE('25-06-1903', 'DD/MM/YYYY'), 'Crítica al totalitarismo'),
('Isabel Allende', 'Chilena', TO_DATE('02-08-1942', 'DD/MM/YYYY'), 'Narrativa latinoamericana'),
('J.K. Rowling', 'Británica', TO_DATE('31-07-1965', 'DD/MM/YYYY'), 'Harry Potter'),
('Haruki Murakami', 'Japonesa', TO_DATE('12-01-1949', 'DD/MM/YYYY'), 'Surrealismo moderno');

INSERT INTO categorias (nombre, descripcion) VALUES
('Ficción', 'Narrativa imaginaria'),
('Ciencia Ficción', 'Futuro y tecnología'),
('Fantasía', 'Mundos mágicos'),
('Drama', 'Conflictos humanos'),
('Misterio', 'Crimen y enigmas');


INSERT INTO libros (titulo, isbn, anio_publicacion, editorial, numero_paginas, idioma, stock, disponible, autor_id, categoria_id) VALUES
('Cien años de soledad', '978-0307474728', 1967, 'Sudamericana', 496, 'es', 5, TRUE, 1, 1),
('1984', '978-0451524935', 1949, 'Secker & Warburg', 328, 'en', 3, TRUE, 2, 2),
('La casa de los espíritus', '978-0307474537', 1982, 'Plaza & Janés', 448, 'es', 4, TRUE, 3, 1),
('Harry Potter y la piedra filosofal', '978-8449306815', 1997, 'Bloomsbury', 256, 'en', 8, TRUE, 4, 3),
('Tokio blues', '978-8483835036', 1987, 'Kodansha', 384, 'es', 2, TRUE, 5, 4);


INSERT INTO usuarios (nombres, apellidos, correo, telefono, direccion) VALUES
('Carlos', 'Mendoza', 'carlos.mendoza@email.com', '+123456789', 'Av. Central'),
('Ana', 'Gómez', 'ana.gomez@email.com', '+987654321', 'Calle Flores'),
('Luis', 'Martínez', 'luis.martinez@email.com', NULL, 'Barrio Centro'),
('Sofía', 'Rodríguez', 'sofia.rod@email.com', '+5037123456', 'Altavista'),
('Diego', 'Pérez', 'diego.perez@email.com', '+5032255888', 'San Benito');


INSERT INTO prestamos (usuario_id, libro_id, fecha_prestamo, fecha_devolucion, estado, observaciones) VALUES
(1, 1, TO_DATE('01-06-2026', 'DD/MM/YYYY'), TO_DATE('15-06-2026', 'DD/MM/YYYY'), 'DEVUELTO', 'Entregado'),
(2, 2, TO_DATE('10-06-2026', 'DD/MM/YYYY'), NULL, 'PRESTADO', 'Prórroga'),
(3, 4, TO_DATE('20-05-2026', 'DD/MM/YYYY'), TO_DATE('03-06-2026', 'DD/MM/YYYY'), 'DEVUELTO', NULL),
(4, 5, TO_DATE('02-06-2026', 'DD/MM/YYYY'), NULL, 'ATRASADO', 'Retraso'),
(5, 3, TO_DATE('25-06-2026', 'DD/MM/YYYY'), NULL, 'PRESTADO', 'Primer préstamo');


-- =========================================================================
-- 5. CONSULTAS (SOLO REFERENCIA)
-- =========================================================================

-- Libros con autor y categoría
SELECT l.id, l.titulo, a.nombre, c.nombre
FROM libros l
JOIN autores a ON l.autor_id = a.id
JOIN categorias c ON l.categoria_id = c.id;

-- Historial de préstamos
SELECT p.id, u.nombres, l.titulo, p.estado
FROM prestamos p
JOIN usuarios u ON p.usuario_id = u.id
JOIN libros l ON p.libro_id = l.id;


-- =========================================================================
-- 6. UTILIDADES (OPCIONAL)
-- =========================================================================

-- TRUNCATE TOTAL
-- TRUNCATE TABLE prestamos, libros, usuarios, categorias, autores RESTART IDENTITY CASCADE;