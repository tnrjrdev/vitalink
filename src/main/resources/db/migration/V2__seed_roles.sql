-- ===========================================================================
-- V2 - Carga inicial dos perfis (RBAC)
-- O usuario administrador inicial e criado em runtime (DataInitializer),
-- pois a senha precisa ser gerada com BCrypt pela aplicacao.
-- UUIDs fixos para idempotencia e referencia previsivel entre ambientes.
-- ===========================================================================
INSERT INTO roles (id, name, description) VALUES
    ('11111111-1111-1111-1111-111111111111', 'ROLE_ADMIN',        'Administrador da plataforma'),
    ('22222222-2222-2222-2222-222222222222', 'ROLE_HOSPITAL',     'Gestor de hospital'),
    ('33333333-3333-3333-3333-333333333333', 'ROLE_CLINIC',       'Gestor de clinica'),
    ('44444444-4444-4444-4444-444444444444', 'ROLE_INSURER',      'Operadora de saude'),
    ('55555555-5555-5555-5555-555555555555', 'ROLE_PROFESSIONAL', 'Profissional de saude'),
    ('66666666-6666-6666-6666-666666666666', 'ROLE_PATIENT',      'Paciente')
ON CONFLICT (name) DO NOTHING;
