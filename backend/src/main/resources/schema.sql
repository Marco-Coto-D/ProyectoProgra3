//-- sin esto las tildes se guardan mal si se carga con docker exec
SET NAMES utf8mb4;

CREATE DATABASE IF NOT EXISTS reservas
    CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE reservas;

CREATE TABLE IF NOT EXISTS categoria_recurso (
    id          VARCHAR(50)  NOT NULL PRIMARY KEY,
    descripcion VARCHAR(150) NOT NULL
) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

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

// -- admin inicial, sin al menos uno no hay forma de entrar
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
