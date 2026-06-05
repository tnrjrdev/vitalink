-- ===========================================================================
-- V1 - Schema inicial da Plataforma Medico
-- Convencoes:
--   * PKs do tipo UUID (nao-enumeravel, seguro para exposicao em URLs)
--   * Timestamps em TIMESTAMPTZ (sempre armazenados em UTC)
--   * Colunas de auditoria e optimistic locking (version) em todas as entidades
--   * Soft-state via coluna "status" (preserva historico / auditabilidade)
-- ===========================================================================

-- ------------------------- RBAC -------------------------
CREATE TABLE roles (
    id          UUID PRIMARY KEY,
    name        VARCHAR(50)  NOT NULL UNIQUE,
    description VARCHAR(255)
);

CREATE TABLE users (
    id          UUID PRIMARY KEY,
    email       VARCHAR(180) NOT NULL UNIQUE,
    password    VARCHAR(120) NOT NULL,
    full_name   VARCHAR(150) NOT NULL,
    status      VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at  TIMESTAMPTZ  NOT NULL,
    updated_at  TIMESTAMPTZ,
    created_by  VARCHAR(180),
    updated_by  VARCHAR(180),
    version     BIGINT       NOT NULL DEFAULT 0
);

CREATE TABLE user_roles (
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES roles (id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- ------------------------- Organizacoes -------------------------
-- Hospital, Clinica e Operadora compartilham os mesmos atributos:
-- usamos uma unica tabela com discriminador "type".
CREATE TABLE organizations (
    id                 UUID PRIMARY KEY,
    legal_name         VARCHAR(180) NOT NULL,
    trade_name         VARCHAR(180),
    cnpj               VARCHAR(14)  NOT NULL UNIQUE,
    type               VARCHAR(20)  NOT NULL,           -- HOSPITAL | CLINIC | INSURER
    email              VARCHAR(180),
    phone              VARCHAR(20),
    status             VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    address_street     VARCHAR(180),
    address_number     VARCHAR(20),
    address_complement VARCHAR(120),
    address_district   VARCHAR(120),
    address_city       VARCHAR(120),
    address_state      VARCHAR(2),
    address_zip_code   VARCHAR(9),
    address_country    VARCHAR(60) DEFAULT 'BR',
    created_at         TIMESTAMPTZ  NOT NULL,
    updated_at         TIMESTAMPTZ,
    created_by         VARCHAR(180),
    updated_by         VARCHAR(180),
    version            BIGINT       NOT NULL DEFAULT 0
);
CREATE INDEX idx_organizations_type ON organizations (type);

-- ------------------------- Profissionais de saude -------------------------
CREATE TABLE healthcare_professionals (
    id              UUID PRIMARY KEY,
    user_id         UUID UNIQUE REFERENCES users (id) ON DELETE SET NULL,
    organization_id UUID NOT NULL REFERENCES organizations (id),
    full_name       VARCHAR(150) NOT NULL,
    cpf             VARCHAR(11)  NOT NULL UNIQUE,
    council_type    VARCHAR(10)  NOT NULL,              -- CRM | CRO | COREN | CRF ...
    council_number  VARCHAR(20)  NOT NULL,
    council_state   VARCHAR(2)   NOT NULL,
    specialty       VARCHAR(120),
    email           VARCHAR(180),
    phone           VARCHAR(20),
    status          VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMPTZ  NOT NULL,
    updated_at      TIMESTAMPTZ,
    created_by      VARCHAR(180),
    updated_by      VARCHAR(180),
    version         BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT uk_professional_council UNIQUE (council_type, council_number, council_state)
);
CREATE INDEX idx_professionals_organization ON healthcare_professionals (organization_id);

-- ------------------------- Pacientes -------------------------
CREATE TABLE patients (
    id                 UUID PRIMARY KEY,
    user_id            UUID UNIQUE REFERENCES users (id) ON DELETE SET NULL,
    full_name          VARCHAR(150) NOT NULL,
    cpf                VARCHAR(11)  NOT NULL UNIQUE,
    birth_date         DATE         NOT NULL,
    gender             VARCHAR(20),
    email              VARCHAR(180),
    phone              VARCHAR(20),
    status             VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    address_street     VARCHAR(180),
    address_number     VARCHAR(20),
    address_complement VARCHAR(120),
    address_district   VARCHAR(120),
    address_city       VARCHAR(120),
    address_state      VARCHAR(2),
    address_zip_code   VARCHAR(9),
    address_country    VARCHAR(60) DEFAULT 'BR',
    created_at         TIMESTAMPTZ  NOT NULL,
    updated_at         TIMESTAMPTZ,
    created_by         VARCHAR(180),
    updated_by         VARCHAR(180),
    version            BIGINT       NOT NULL DEFAULT 0
);

-- ------------------------- Planos de saude (Operadora) -------------------------
CREATE TABLE insurance_plans (
    id            UUID PRIMARY KEY,
    operator_id   UUID NOT NULL REFERENCES organizations (id),
    name          VARCHAR(150) NOT NULL,
    ans_code      VARCHAR(20)  NOT NULL UNIQUE,         -- registro ANS
    coverage_type VARCHAR(30)  NOT NULL,                -- AMBULATORIAL | HOSPITALAR | ODONTOLOGICO | COMPLETO
    status        VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at    TIMESTAMPTZ  NOT NULL,
    updated_at    TIMESTAMPTZ,
    created_by    VARCHAR(180),
    updated_by    VARCHAR(180),
    version       BIGINT       NOT NULL DEFAULT 0
);
CREATE INDEX idx_insurance_plans_operator ON insurance_plans (operator_id);

-- ------------------------- Consultas / Agendamentos -------------------------
CREATE TABLE appointments (
    id                UUID PRIMARY KEY,
    patient_id        UUID NOT NULL REFERENCES patients (id),
    professional_id   UUID NOT NULL REFERENCES healthcare_professionals (id),
    organization_id   UUID NOT NULL REFERENCES organizations (id),
    insurance_plan_id UUID REFERENCES insurance_plans (id),
    scheduled_start   TIMESTAMPTZ NOT NULL,
    scheduled_end     TIMESTAMPTZ NOT NULL,
    status            VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',  -- SCHEDULED|CONFIRMED|COMPLETED|CANCELLED|NO_SHOW
    type              VARCHAR(20) NOT NULL DEFAULT 'IN_PERSON',  -- IN_PERSON | TELEMEDICINE
    reason            VARCHAR(500),
    notes             TEXT,
    created_at        TIMESTAMPTZ NOT NULL,
    updated_at        TIMESTAMPTZ,
    created_by        VARCHAR(180),
    updated_by        VARCHAR(180),
    version           BIGINT      NOT NULL DEFAULT 0,
    CONSTRAINT chk_appointment_period CHECK (scheduled_end > scheduled_start)
);
CREATE INDEX idx_appointments_professional_start ON appointments (professional_id, scheduled_start);
CREATE INDEX idx_appointments_patient ON appointments (patient_id);
CREATE INDEX idx_appointments_organization ON appointments (organization_id);
CREATE INDEX idx_appointments_status ON appointments (status);
