-- ===========================================================================
-- V3 - Documentos clinicos (metadados no banco; arquivo binario no S3)
-- Mesmas convencoes do schema inicial: PK UUID, TIMESTAMPTZ em UTC,
-- colunas de auditoria + optimistic locking, soft-state via "status".
-- ===========================================================================
CREATE TABLE documents (
    id             UUID PRIMARY KEY,
    patient_id     UUID NOT NULL REFERENCES patients (id),
    appointment_id UUID REFERENCES appointments (id),
    file_name      VARCHAR(255) NOT NULL,
    content_type   VARCHAR(120),
    size_bytes     BIGINT,
    storage_key    VARCHAR(512) NOT NULL,
    description    VARCHAR(500),
    status         VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at     TIMESTAMPTZ  NOT NULL,
    updated_at     TIMESTAMPTZ,
    created_by     VARCHAR(180),
    updated_by     VARCHAR(180),
    version        BIGINT       NOT NULL DEFAULT 0
);
CREATE INDEX idx_documents_patient ON documents (patient_id);
CREATE INDEX idx_documents_appointment ON documents (appointment_id);
