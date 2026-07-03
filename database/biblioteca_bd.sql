-- =========================================================================
-- 1. ENUMS Y FUNCIONES GLOBALES
-- =========================================================================

-- Reemplazamos el CHECK de texto por un ENUM nativo. Es mucho más rápido de indexar y consume menos espacio.
CREATE TYPE estado_prestamo AS ENUM ('PRESTADO', 'DEVUELTO', 'ATRASADO');

-- Función para actualizar automáticamente el campo updated_at en las modificaciones
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

CREATE TABLE autores (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(120) NOT NULL,
    nacionalidad VARCHAR(80),
    fecha_nacimiento DATE,
    biografia TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP, -- Usar TIMESTAMPTZ guarda la zona horaria global
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TRIGGER set_timestamp_autores
    BEFORE UPDATE ON autores
    FOR EACH ROW EXECUTE FUNCTION trigger_set_timestamp();


CREATE TABLE categorias (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(80) NOT NULL UNIQUE,
    descripcion TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TRIGGER set_timestamp_categorias
    BEFORE UPDATE ON categorias
    FOR EACH ROW EXECUTE FUNCTION trigger_set_timestamp();


CREATE TABLE libros (
    id BIGSERIAL PRIMARY KEY,
    titulo VARCHAR(200) NOT NULL,
    isbn VARCHAR(20) NOT NULL UNIQUE, -- Ya genera un índice único implícito
    anio_publicacion SMALLINT CHECK (anio_publicacion > 0 AND anio_publicacion <= EXTRACT(YEAR FROM CURRENT_DATE)), -- Optimización de tipo y check de año real
    editorial VARCHAR(120),
    numero_paginas INT CHECK (numero_paginas > 0), -- Integridad: No existen páginas negativas o 0
    idioma VARCHAR(10), -- Optimizado para códigos ISO (ej: 'es', 'en', 'fr')
    stock INT NOT NULL DEFAULT 0 CHECK (stock >= 0), -- Integridad: El stock no puede ser menor a 0
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


CREATE TABLE usuarios (
    id BIGSERIAL PRIMARY KEY,
    nombres VARCHAR(120) NOT NULL,
    apellidos VARCHAR(120) NOT NULL,
    correo VARCHAR(120) NOT NULL UNIQUE, -- Ya genera un índice único implícito
    telefono VARCHAR(20),
    direccion TEXT,
    fecha_registro TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    activo BOOLEAN DEFAULT TRUE
);


CREATE TABLE prestamos (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    libro_id BIGINT NOT NULL,
    fecha_prestamo DATE NOT NULL DEFAULT CURRENT_DATE,
    fecha_devolucion DATE,
    estado estado_prestamo NOT NULL DEFAULT 'PRESTADO', -- Uso del ENUM optimizado
    observaciones TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_prestamo_usuario FOREIGN KEY(usuario_id) REFERENCES usuarios(id) ON DELETE RESTRICT,
    CONSTRAINT fk_prestamo_libro FOREIGN KEY(libro_id) REFERENCES libros(id) ON DELETE RESTRICT,
    CONSTRAINT chk_fechas_prestamo CHECK (fecha_devolucion >= fecha_prestamo) -- Integridad: No puedes devolverlo antes de pedirlo
);


-- =========================================================================
-- 3. ÍNDICES ESTRATÉGICOS (ELIMINADOS LOS REPERCUSIVOS Y AGREGADOS LOS DE FK)
-- =========================================================================

-- Para acelerar los JOINs (Cruciales en rendimiento)
CREATE INDEX idx_libros_autor_id ON libros(autor_id);
CREATE INDEX idx_libros_categoria_id ON libros(categoria_id);
CREATE INDEX idx_prestamos_usuario_id ON prestamos(usuario_id);
CREATE INDEX idx_prestamos_libro_id ON prestamos(libro_id);

-- Para búsquedas frecuentes de texto
CREATE INDEX idx_libros_titulo ON libros(titulo);

-- ÍNDICE PARCIAL: En lugar de indexar millones de préstamos devueltos del pasado,
-- este índice solo guardará los que están activos o atrasados. ¡Velocidad pura para tu sistema!
CREATE INDEX idx_prestamos_activos ON prestamos(estado) WHERE estado IN ('PRESTADO', 'ATRASADO');



-- =========================================================================
-- 1. INSERCIÓN EN AUTORES (Formato DD/MM/YYYY aplicado a fecha_nacimiento)
-- =========================================================================
INSERT INTO autores (nombre, nacionalidad, fecha_nacimiento, biografia) VALUES
('Gabriel García Márquez', 'Colombiana', TO_DATE('06-03-1927', 'DD/MM/YYYY'), 'Escritor emblemático del realismo mágico, ganador del Premio Nobel de Literatura en 1982.'),
('George Orwell', 'Británica', TO_DATE('25-06-1903', 'DD/MM/YYYY'), 'Seudónimo de Eric Arthur Blair, novelista y periodista británico, famoso por sus críticas al totalitarismo.'),
('Isabel Allende', 'Chilena', TO_DATE('02-08-1942', 'DD/MM/YYYY'), 'Escritora y periodista superventas, conocida por obras de gran éxito internacional basadas en drama y realismo.'),
('J.K. Rowling', 'Británica', TO_DATE('31-07-1965', 'DD/MM/YYYY'), 'Escritora, productora de cine y guionista, mundialmente conocida por ser la autora de la saga Harry Potter.'),
('Haruki Murakami', 'Japonesa', TO_DATE('12-01-1949', 'DD/MM/YYYY'), 'Escritor de novelas surrealistas y melancólicas que mezclan la cultura pop occidental con el misticismo japonés.');

-- =========================================================================
-- 2. INSERCIÓN EN CATEGORÍAS
-- =========================================================================
INSERT INTO categorias (nombre, descripcion) VALUES
('Ficción', 'Obras literarias que narran hechos imaginarios o basados en la realidad pero reinterpretados.'),
('Ciencia Ficción', 'Relatos que especulan sobre desarrollos científicos, tecnológicos o futuros distópicos.'),
('Fantasía', 'Libros con elementos mágicos, mitológicos o mundos completamente inventados.'),
('Drama', 'Historias que profundizan en los conflictos emocionales y psychological de los personajes.'),
('Misterio', 'Novelas centradas en la resolución de un crimen, enigma o secreto.');

-- =========================================================================
-- 3. INSERCIÓN EN LIBROS
-- =========================================================================
INSERT INTO libros (titulo, isbn, anio_publicacion, editorial, numero_paginas, idioma, stock, disponible, autor_id, categoria_id) VALUES
('Cien años de soledad', '978-0307474728', 1967, 'Editorial Sudamericana', 496, 'es', 5, TRUE, 1, 1),
('1984', '978-0451524935', 1949, 'Secker & Warburg', 328, 'en', 3, TRUE, 2, 2),
('La casa de los espíritus', '978-0307474537', 1982, 'Plaza & Janés', 448, 'es', 4, TRUE, 3, 1),
('Harry Potter y la piedra filosofal', '978-8449306815', 1997, 'Bloomsbury', 256, 'en', 8, TRUE, 4, 3),
('Tokio blues', '978-8483835036', 1987, 'Kodansha', 384, 'es', 2, TRUE, 5, 4);

-- =========================================================================
-- 4. INSERCIÓN EN USUARIOS
-- =========================================================================
INSERT INTO usuarios (nombres, apellidos, correo, telefono, direccion) VALUES
('Carlos', 'Mendoza', 'carlos.mendoza@email.com', '+123456789', 'Av. De la Independencia 123'),
('Ana', 'Gómez', 'ana.gomez@email.com', '+987654321', 'Calle de las Flores 456'),
('Luis', 'Martínez', 'luis.martinez@email.com', NULL, 'Barrio El Centro, Bloque B'),
('Sofía', 'Rodríguez', 'sofia.rod@email.com', '+5037123456', 'Condominio Altavista, Apto 4B'),
('Diego', 'Pérez', 'diego.perez@email.com', '+5032255888', 'Residencial San Benito, Calle principal');

-- =========================================================================
-- 5. INSERCIÓN EN PRÉSTAMOS (Formato DD/MM/YYYY aplicado a las fechas)
-- =========================================================================
INSERT INTO prestamos (usuario_id, libro_id, fecha_prestamo, fecha_devolucion, estado, observaciones) VALUES
(1, 1, TO_DATE('01-06-2026', 'DD/MM/YYYY'), TO_DATE('15-06-2026', 'DD/MM/YYYY'), 'DEVUELTO', 'Entregado a tiempo y en excelentes condiciones.'),
(2, 2, TO_DATE('10-06-2026', 'DD/MM/YYYY'), NULL, 'PRESTADO', 'Usuario solicitó prórroga de 3 días.'),
(3, 4, TO_DATE('20-05-2026', 'DD/MM/YYYY'), TO_DATE('03-06-2026', 'DD/MM/YYYY'), 'DEVUELTO', NULL),
(4, 5, TO_DATE('02-06-2026', 'DD/MM/YYYY'), NULL, 'ATRASADO', 'Se envió correo de notificación de retraso.'),
(5, 3, TO_DATE('25-06-2026', 'DD/MM/YYYY'), NULL, 'PRESTADO', 'Primer préstamo de este usuario.');


--A) Ver los libros con su Autor y Categoría correspondientes:--
SELECT 
    l.id AS libro_id,
    l.titulo,
    l.isbn,
    a.nombre AS autor,
    c.nombre AS categoria,
    l.stock,
    l.disponible
FROM libros l
JOIN autores a ON l.autor_id = a.id
JOIN categorias c ON l.categoria_id = c.id
ORDER BY l.titulo ASC;

--B) Ver el historial de Préstamos (Quién se llevó qué, cuándo y en qué estado está):--
SELECT 
    p.id AS prestamo_id,
    CONCAT(u.nombres, ' ', u.apellidos) AS usuario,
    u.correo,
    l.titulo AS libro,
    p.fecha_prestamo,
    p.fecha_devolucion,
    p.estado
FROM prestamos p
JOIN usuarios u ON p.usuario_id = u.id
JOIN libros l ON p.libro_id = l.id
ORDER BY p.fecha_prestamo DESC;


TRUNCATE TABLE 
    prestamos, 
    libros, 
    usuarios, 
    categorias, 
    autores 
RESTART IDENTITY CASCADE;


SELECT 
    p.id AS prestamo_id,
    CONCAT(u.nombres, ' ', u.apellidos) AS usuario,
    u.correo,
    l.titulo AS libro,
    -- Aquí forzamos la visualización en pantalla:
    TO_CHAR(p.fecha_prestamo, 'DD/MM/YYYY') AS fecha_prestamo,
    COALESCE(TO_CHAR(p.fecha_devolucion, 'DD/MM/YYYY'), 'Pendiente') AS fecha_devolucion,
    p.estado
FROM prestamos p
JOIN usuarios u ON p.usuario_id = u.id
JOIN libros l ON p.libro_id = l.id
ORDER BY p.id DESC;



SELECT tablename
FROM pg_tables
WHERE schemaname='public';


SELECT DISTINCT estado
FROM prestamos;


CREATE TYPE estado_prestamo AS ENUM (
    'PRESTADO',
    'DEVUELTO',
    'ATRASADO'
);

SELECT column_name, data_type, udt_name
FROM information_schema.columns
WHERE table_name = 'prestamos'
AND column_name = 'estado';

ALTER TABLE prestamos
ALTER COLUMN estado
TYPE estado_prestamo
USING estado::estado_prestamo;

SELECT DISTINCT estado
FROM prestamos;

SELECT column_name, data_type, udt_name
FROM information_schema.columns
WHERE table_name = 'prestamos'
AND column_name = 'estado';