-- Script de inicialización para PostgreSQL
-- Ejecutar este script en tu servidor PostgreSQL local

-- Conectarse como superusuario y crear la base de datos
-- psql -U postgres
-- CREATE DATABASE motel_management_db;
-- \c motel_management_db

-- O desde la terminal:
-- psql -U postgres -c "CREATE DATABASE motel_management_db;"
-- psql -U postgres -d motel_management_db -f postgres-init-motel.sql

-- Eliminar tablas si existen (para re-ejecución limpia)
DROP TABLE IF EXISTS room_service CASCADE;
DROP TABLE IF EXISTS room_image CASCADE;
DROP TABLE IF EXISTS service CASCADE;
DROP TABLE IF EXISTS room CASCADE;
DROP TABLE IF EXISTS motel CASCADE;

-- Tabla de moteles
CREATE TABLE motel (
                       id BIGSERIAL PRIMARY KEY,
                       name VARCHAR(100) NOT NULL,
                       address VARCHAR(255) NOT NULL,
                       phone_number VARCHAR(20),
                       description VARCHAR(500),
                       city VARCHAR(100) NOT NULL,
                       property_id BIGINT,
                       date_created TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Índices para motel
CREATE INDEX idx_motel_city ON motel(city);
CREATE INDEX idx_motel_property ON motel(property_id);

-- Tabla de habitaciones
CREATE TABLE room (
                      id BIGSERIAL PRIMARY KEY,
                      motel_id BIGINT NOT NULL,
                      number VARCHAR(20) NOT NULL,
                      room_type VARCHAR(50) NOT NULL,
                      price NUMERIC(10,2) NOT NULL,
                      description VARCHAR(500),
                      is_available BOOLEAN DEFAULT TRUE,
                      FOREIGN KEY (motel_id) REFERENCES motel(id) ON DELETE CASCADE,
                      UNIQUE (motel_id, number)
);

-- Índices para room
CREATE INDEX idx_room_motel ON room(motel_id);
CREATE INDEX idx_room_available ON room(is_available);

-- Tabla de imágenes de habitaciones
CREATE TABLE room_image (
                            id BIGSERIAL PRIMARY KEY,
                            room_id BIGINT NOT NULL,
                            image_url VARCHAR(500) NOT NULL,
                            display_order INTEGER NOT NULL,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            FOREIGN KEY (room_id) REFERENCES room(id) ON DELETE CASCADE
);

-- Índices para room_image
CREATE INDEX idx_room_image_room ON room_image(room_id);
CREATE INDEX idx_room_image_order ON room_image(room_id, display_order);

-- Tabla de servicios
CREATE TABLE service (
                         id BIGSERIAL PRIMARY KEY,
                         name VARCHAR(50) NOT NULL UNIQUE,
                         description VARCHAR(255),
                         icon VARCHAR(50),
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Índice para service
CREATE INDEX idx_service_name ON service(name);

-- Tabla intermedia para la relación muchos-a-muchos entre room y service
CREATE TABLE room_service (
                              room_id BIGINT NOT NULL,
                              service_id BIGINT NOT NULL,
                              PRIMARY KEY (room_id, service_id),
                              FOREIGN KEY (room_id) REFERENCES room(id) ON DELETE CASCADE,
                              FOREIGN KEY (service_id) REFERENCES service(id) ON DELETE CASCADE
);

-- Índices para room_service
CREATE INDEX idx_room_service_room ON room_service(room_id);
CREATE INDEX idx_room_service_service ON room_service(service_id);

-- Insertar servicios predefinidos
INSERT INTO service (name, description, icon) VALUES
                                                  ('Jacuzzi', 'Jacuzzi privado en la habitación', 'hot_tub'),
                                                  ('Spa', 'Acceso a spa y sauna', 'spa'),
                                                  ('WiFi', 'Internet de alta velocidad', 'wifi'),
                                                  ('TV Cable', 'Televisión por cable premium', 'tv'),
                                                  ('Minibar', 'Minibar completamente equipado', 'local_bar'),
                                                  ('Aire Acondicionado', 'Climatización completa', 'ac_unit'),
                                                  ('Estacionamiento', 'Garaje privado', 'local_parking'),
                                                  ('Room Service', 'Servicio a la habitación 24/7', 'room_service'),
                                                  ('Cama King', 'Cama tamaño King', 'king_bed'),
                                                  ('Vista al Mar', 'Habitación con vista al mar', 'beach_access'),
                                                  ('Balcón', 'Balcón privado', 'balcony'),
                                                  ('Cocina', 'Cocina equipada', 'kitchen')
    ON CONFLICT (name) DO NOTHING;

-- Datos de ejemplo para moteles
INSERT INTO motel (name, address, phone_number, description, city, property_id) VALUES
                                                                                    ('Motel Paraíso', 'Calle 10 #15-20', '555-0100', 'Motel romántico con temática tropical', 'Medellín', 1),
                                                                                    ('Motel Las Estrellas', 'Avenida 80 #50-30', '555-0200', 'Experiencia de lujo y privacidad', 'Medellín', 1),
                                                                                    ('Motel El Oasis', 'Carrera 70 #45-12', '555-0300', 'Tranquilidad y confort en plena ciudad', 'Bogotá', 2)
    ON CONFLICT DO NOTHING;

-- Habitaciones de ejemplo
INSERT INTO room (motel_id, number, room_type, price, description, is_available) VALUES
-- Motel Paraíso (id=1)
(1, '101', 'Suite Ejecutiva', 150000.00, 'Suite con jacuzzi y sala de estar', TRUE),
(1, '102', 'Suite Romántica', 180000.00, 'Ambiente romántico con jacuzzi doble', TRUE),
(1, '103', 'Suite Presidencial', 250000.00, 'La mejor suite con todas las amenidades', TRUE),
-- Motel Las Estrellas (id=2)
(2, '201', 'Suite VIP', 200000.00, 'Suite VIP con vista panorámica', TRUE),
(2, '202', 'Suite Deluxe', 220000.00, 'Suite deluxe con spa privado', TRUE),
(2, '203', 'Suite Imperial', 280000.00, 'Lujo supremo y privacidad absoluta', FALSE),
-- Motel El Oasis (id=3)
(3, '301', 'Suite Estándar', 120000.00, 'Comodidad y buen precio', TRUE),
(3, '302', 'Suite Premium', 170000.00, 'Elegancia y confort', TRUE)
    ON CONFLICT (motel_id, number) DO NOTHING;

-- Asociar servicios a habitaciones
-- Suite Ejecutiva (id=1)
INSERT INTO room_service (room_id, service_id) VALUES
                                                   (1, 1), (1, 3), (1, 4), (1, 5), (1, 6)
    ON CONFLICT DO NOTHING;

-- Suite Romántica (id=2)
INSERT INTO room_service (room_id, service_id) VALUES
                                                   (2, 1), (2, 2), (2, 3), (2, 4), (2, 5), (2, 6), (2, 11)
    ON CONFLICT DO NOTHING;

-- Suite Presidencial (id=3)
INSERT INTO room_service (room_id, service_id) VALUES
                                                   (3, 1), (3, 2), (3, 3), (3, 4), (3, 5), (3, 6), (3, 7), (3, 8), (3, 9), (3, 11)
    ON CONFLICT DO NOTHING;

-- Suite VIP (id=4)
INSERT INTO room_service (room_id, service_id) VALUES
                                                   (4, 1), (4, 3), (4, 4), (4, 5), (4, 6), (4, 7), (4, 10)
    ON CONFLICT DO NOTHING;

-- Suite Deluxe (id=5)
INSERT INTO room_service (room_id, service_id) VALUES
                                                   (5, 1), (5, 2), (5, 3), (5, 4), (5, 5), (5, 6), (5, 7), (5, 8), (5, 10), (5, 11)
    ON CONFLICT DO NOTHING;

-- Suite Imperial (id=6)
INSERT INTO room_service (room_id, service_id) VALUES
                                                   (6, 1), (6, 2), (6, 3), (6, 4), (6, 5), (6, 6), (6, 7), (6, 8), (6, 9), (6, 10), (6, 11), (6, 12)
    ON CONFLICT DO NOTHING;

-- Suite Estándar (id=7)
INSERT INTO room_service (room_id, service_id) VALUES
                                                   (7, 3), (7, 4), (7, 6)
    ON CONFLICT DO NOTHING;

-- Suite Premium (id=8)
INSERT INTO room_service (room_id, service_id) VALUES
                                                   (8, 1), (8, 3), (8, 4), (8, 5), (8, 6), (8, 11)
    ON CONFLICT DO NOTHING;

-- Verificación de datos insertados
SELECT 'Moteles insertados:' as info, COUNT(*) as cantidad FROM motel;
SELECT 'Habitaciones insertadas:' as info, COUNT(*) as cantidad FROM room;
SELECT 'Servicios insertados:' as info, COUNT(*) as cantidad FROM service;
SELECT 'Relaciones room-service:' as info, COUNT(*) as cantidad FROM room_service;

-- Script completado exitosamente
SELECT '¡Base de datos inicializada correctamente!' as mensaje;