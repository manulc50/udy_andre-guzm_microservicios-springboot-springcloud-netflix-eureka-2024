-- Este archivo .sql se ejecuta automáticamente si la propiedad de Hibernate "spring.jpa.hibernate.ddl-auto" tiene el valor "create" o "create-drop"
-- Datos de ejemplo de usuarios - Las passwords encriptadas con BCrypt han sido generadas a partir de la password "12345"
INSERT INTO usuarios (username, password, enabled, nombre, apellido, email) VALUES ('andres','$2a$10$ykhXmCAam5FUEF9GN.4Z8OwwWJidvMii6VFYe77cmS2X6oF6p4W86',true, 'Andres', 'Guzman','profesor@bolsadeideas.com');
INSERT INTO usuarios (username, password, enabled, nombre, apellido, email) VALUES ('admin','$2a$10$qGyDfZLBB.SgLv7GCP3uZe3oM38fVtr58T1iZ1LNOvO61loNUAAaK',true, 'John', 'Doe','jhon.doe@bolsadeideas.com');

-- Datos de ejemplo de roles
INSERT INTO roles (nombre) VALUES ('ROLE_USER');
INSERT INTO roles (nombre) VALUES ('ROLE_ADMIN');

-- Datos de ejemplo de relaciones entre usuarios y roles
INSERT INTO usuarios_roles (usuario_id, role_id) VALUES (1, 1);
INSERT INTO usuarios_roles (usuario_id, role_id) VALUES (2, 2);
INSERT INTO usuarios_roles (usuario_id, role_id) VALUES (2, 1);