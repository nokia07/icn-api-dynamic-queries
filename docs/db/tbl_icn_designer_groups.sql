-- Grupos con acceso al diseñador de consultas dinámicas (feature DQ del plug-in de ICN).
-- La API icn-api-dynamic-queries (DesignerAccessInterceptor) los compara con los grupos del usuario en WebSphere antes
-- de crear, modificar o eliminar consultas, proyectos o asociaciones; leer no los requiere.
-- Los cambios aplican en la siguiente solicitud (no hay caché). Sin filas ACTIVE nadie puede usar el diseñador.
-- Debe crearse en el mismo esquema que tbl_icn_projects (el del usuario del datasource jdbc/GeneraConsDinaDS).

CREATE TABLE tbl_icn_designer_groups (
    dgr_id          NUMBER GENERATED ALWAYS AS IDENTITY,
    dgr_name        VARCHAR2(512)                     NOT NULL,
    dgr_description VARCHAR2(100),
    dgr_status      VARCHAR2(10)  DEFAULT 'ACTIVE'    NOT NULL,
    dgr_created_at  TIMESTAMP     DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT pk_icn_designer_groups PRIMARY KEY (dgr_id),
    CONSTRAINT ck_icn_designer_groups_status CHECK (dgr_status IN ('ACTIVE', 'INACTIVE'))
);

-- La comparación no distingue mayúsculas: tampoco se admiten nombres que solo difieren en ellas.
CREATE UNIQUE INDEX uq_icn_designer_groups_name ON tbl_icn_designer_groups (UPPER(dgr_name));

COMMENT ON TABLE tbl_icn_designer_groups IS 'Grupos que pueden crear, modificar y eliminar en la API icn-api-dynamic-queries (diseñador de consultas dinámicas)';
COMMENT ON COLUMN tbl_icn_designer_groups.dgr_id IS 'Identificador';
COMMENT ON COLUMN tbl_icn_designer_groups.dgr_name IS 'Grupo del directorio: nombre corto (CN) o DN completo; sin distinguir mayúsculas';
COMMENT ON COLUMN tbl_icn_designer_groups.dgr_description IS 'Descripción opcional';
COMMENT ON COLUMN tbl_icn_designer_groups.dgr_status IS 'ACTIVE: da acceso; INACTIVE: se ignora';
COMMENT ON COLUMN tbl_icn_designer_groups.dgr_created_at IS 'Fecha de creación';

-- Ejemplo:
-- INSERT INTO tbl_icn_designer_groups (dgr_name, dgr_description) VALUES ('DQ_Disenadores', 'Diseñadores de consultas');
-- COMMIT;
