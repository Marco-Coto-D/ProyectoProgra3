-- Esquema de la base de datos "reservas".
-- Tablas: categoria_recurso, funcionario, administrador, recurso, reserva
-- y reserva_recurso (los recursos asignados a cada reserva).

-- Forzar utf8mb4 en la sesión del cliente: sin esto, si el cliente mysql
-- arranca en latin1 (pasa al hacer `docker exec ... mysql < schema.sql`),
-- las tildes de este archivo se guardan doble-codificadas ("CategorÃ­a").
SET NAMES utf8mb4;

CREATE DATABASE IF NOT EXISTS reservas
    CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE reservas;

CREATE TABLE IF NOT EXISTS categoria_recurso (
    id          VARCHAR(50)  NOT NULL PRIMARY KEY,
    descripcion VARCHAR(150) NOT NULL
) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

-- Fila de prueba para validar el viaje de punta a punta del Paso 2.
INSERT INTO categoria_recurso (id, descripcion)
VALUES ('CAT-PRUEBA', 'Categoría de prueba (Paso 2)')
ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion);

CREATE TABLE IF NOT EXISTS funcionario (
    id       VARCHAR(50)  NOT NULL PRIMARY KEY,
    clave    VARCHAR(100) NOT NULL,
    nombre   VARCHAR(150) NOT NULL,
    telefono VARCHAR(30)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS administrador (
    id    VARCHAR(50)  NOT NULL PRIMARY KEY,
    clave VARCHAR(100) NOT NULL
) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

-- Administrador inicial: el mismo que había en data/sistema.xml (id 999, clave 999).
-- Sin al menos uno no hay forma de entrar al sistema para crear el resto.
INSERT INTO administrador (id, clave) VALUES ('999', '999')
ON DUPLICATE KEY UPDATE clave = clave;

CREATE TABLE IF NOT EXISTS recurso (
    id          VARCHAR(50)  NOT NULL PRIMARY KEY,
    descripcion VARCHAR(150) NOT NULL,
    categoria   VARCHAR(50)  NOT NULL,
    FOREIGN KEY (categoria) REFERENCES categoria_recurso(id)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS reserva (
    id          VARCHAR(50)  NOT NULL PRIMARY KEY,
    actividad   VARCHAR(200) NOT NULL,
    fecha       DATE         NOT NULL,
    hora_inicio TIME         NOT NULL,
    hora_fin    TIME         NOT NULL,
    funcionario VARCHAR(50)  NOT NULL,
    estado      VARCHAR(20)  NOT NULL,
    FOREIGN KEY (funcionario) REFERENCES funcionario(id)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS reserva_recurso (
    reserva VARCHAR(50) NOT NULL,
    recurso VARCHAR(50) NOT NULL,
    PRIMARY KEY (reserva, recurso),
    FOREIGN KEY (reserva) REFERENCES reserva(id),
    FOREIGN KEY (recurso) REFERENCES recurso(id)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
