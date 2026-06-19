CREATE DATABASE hubble_db;
CREATE SCHEMA hubble_db;

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TYPE user_role AS ENUM (
    'DOCTOR',
    'PATIENT',
    'CLINIC_STAFF',
    'ADMIN'
    );

CREATE TYPE speciality_type AS ENUM (
    'GENERAL',
    'RETINA',
    'GLAUCOMA',
    'REFRACTIVE'
    );

CREATE TYPE appointment_status AS ENUM (
    'SCHEDULED',
    'CONFIRMED',
    'ATTENDED',
    'CANCELLED'
    );

CREATE TYPE payment_method AS ENUM (
    'CASH',
    'CARD',
    'INSURANCE',
    'YAPE'
    );

CREATE TYPE payment_status AS ENUM (
    'PENDING',
    'PAID',
    'CANCELLED'
    );

CREATE TYPE exam_type AS ENUM (
    'REFRACTION',
    'FUNDUS',
    'TONOMETRY'
    );

CREATE TYPE notification_type AS ENUM (
    'APPOINTMENT_RESERVED',
    'USER_ACCOUNT_CREATED',
    'PAYMENT_COMPLETED'
    );

CREATE TABLE hubble_db.stored_files
(
    id              UUID PRIMARY KEY      DEFAULT uuid_generate_v4(),
    original_name   VARCHAR(255) NOT NULL,
    content_type    VARCHAR(100) NOT NULL,
    size_bytes      BIGINT       NOT NULL,
    storage_path    VARCHAR(500) NOT NULL,
    checksum_sha256 CHAR(64)     NOT NULL DEFAULT '',
    created_at      TIMESTAMP(3)          DEFAULT CURRENT_TIMESTAMP(3)
);

CREATE TABLE hubble_db.users
(
    id              UUID PRIMARY KEY      DEFAULT uuid_generate_v4(),
    email           VARCHAR(150) NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,
    full_name       VARCHAR(100) NOT NULL,
    phone           VARCHAR(20)  NOT NULL,
    role            user_role    NOT NULL,
    active          BOOLEAN               DEFAULT TRUE,
    document_number VARCHAR(20),
    birth_date      DATE,
    specialty       speciality_type,
    avatar_file_id  UUID,
    created_at      TIMESTAMP(3)          DEFAULT CURRENT_TIMESTAMP(3),
    updated_at      TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    deleted_at      TIMESTAMP(3),
    CONSTRAINT fk_user_stored_file FOREIGN KEY (avatar_file_id) REFERENCES hubble_db.stored_files (id)
);

CREATE UNIQUE INDEX uq_users_email_active ON hubble_db.users (email) WHERE deleted_at IS NULL;
CREATE UNIQUE INDEX uq_users_document ON hubble_db.users (document_number) WHERE deleted_at IS NULL AND document_number IS NOT NULL;
CREATE INDEX idx_users_avatar_file ON hubble_db.users (avatar_file_id);

CREATE TABLE hubble_db.user_sessions
(
    id                 UUID PRIMARY KEY      DEFAULT uuid_generate_v4(),
    user_id            UUID         NOT NULL,
    refresh_token_hash VARCHAR(255) NOT NULL,
    access_token_jti   VARCHAR(64)  NOT NULL,
    user_agent         VARCHAR(512) NOT NULL,
    ip_address         VARCHAR(45)  NOT NULL,
    issued_at          TIMESTAMP(3)          DEFAULT CURRENT_TIMESTAMP(3),
    expires_at         TIMESTAMP(3) NOT NULL,
    last_activity_at   TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    revoked_at         TIMESTAMP(3),
    active             BOOLEAN               DEFAULT TRUE,
    created_at         TIMESTAMP(3)          DEFAULT CURRENT_TIMESTAMP(3),
    updated_at         TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    deleted_at         TIMESTAMP(3),
    CONSTRAINT fk_user_sessions_user FOREIGN KEY (user_id) REFERENCES hubble_db.users (id)
);

CREATE INDEX idx_user_sessions_user_id ON hubble_db.user_sessions (user_id);
CREATE UNIQUE INDEX uq_user_sessions_refresh_hash ON hubble_db.user_sessions (refresh_token_hash)
    WHERE revoked_at IS NULL AND deleted_at IS NULL;

CREATE TABLE hubble_db.password_resets
(
    id         UUID PRIMARY KEY      DEFAULT uuid_generate_v4(),
    user_id    UUID         NOT NULL,
    code       CHAR(6)      NOT NULL,
    expires_at TIMESTAMP(3) NOT NULL,
    used       BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP(3)          DEFAULT CURRENT_TIMESTAMP(3),
    CONSTRAINT fk_password_resets_user FOREIGN KEY (user_id) REFERENCES hubble_db.users (id)
);

CREATE INDEX idx_password_resets_user ON hubble_db.password_resets (user_id);
CREATE INDEX idx_password_resets_expires ON hubble_db.password_resets (expires_at);

CREATE TABLE hubble_db.appointments
(
    id               UUID PRIMARY KEY      DEFAULT uuid_generate_v4(),
    user_id          UUID         NOT NULL,
    doctor_id        UUID         NOT NULL,
    appointment_date TIMESTAMP(3) NOT NULL,
    status           appointment_status    DEFAULT 'SCHEDULED',
    reason           TEXT,
    active           BOOLEAN               DEFAULT TRUE,
    created_at       TIMESTAMP(3)          DEFAULT CURRENT_TIMESTAMP(3),
    updated_at       TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    deleted_at       TIMESTAMP(3),
    CONSTRAINT fk_appointment_user FOREIGN KEY (user_id) REFERENCES hubble_db.users (id),
    CONSTRAINT fk_appointment_doctor FOREIGN KEY (doctor_id) REFERENCES hubble_db.users (id)
);

CREATE TABLE hubble_db.medical_consultations
(
    id                   UUID PRIMARY KEY      DEFAULT uuid_generate_v4(),
    appointment_id       UUID         NOT NULL,
    visual_acuity        VARCHAR(50),
    intraocular_pressure VARCHAR(50),
    diagnosis            TEXT,
    notes                TEXT,
    active               BOOLEAN               DEFAULT TRUE,
    created_at           TIMESTAMP(3)          DEFAULT CURRENT_TIMESTAMP(3),
    updated_at           TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    deleted_at           TIMESTAMP(3),
    CONSTRAINT fk_consultation_appointment FOREIGN KEY (appointment_id) REFERENCES hubble_db.appointments (id)
);

CREATE TABLE hubble_db.exams
(
    id              UUID PRIMARY KEY      DEFAULT uuid_generate_v4(),
    user_id         UUID         NOT NULL,
    consultation_id UUID,
    type            exam_type    NOT NULL,
    result          TEXT,
    active          BOOLEAN               DEFAULT TRUE,
    created_at      TIMESTAMP(3)          DEFAULT CURRENT_TIMESTAMP(3),
    updated_at      TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    deleted_at      TIMESTAMP(3),
    CONSTRAINT fk_exam_user FOREIGN KEY (user_id) REFERENCES hubble_db.users (id),
    CONSTRAINT fk_exam_consultation FOREIGN KEY (consultation_id) REFERENCES hubble_db.medical_consultations (id)
);

CREATE TABLE hubble_db.prescriptions
(
    id              UUID PRIMARY KEY      DEFAULT uuid_generate_v4(),
    consultation_id UUID         NOT NULL,
    description     TEXT,
    lens_type       VARCHAR(100),
    recommendations TEXT,
    active          BOOLEAN               DEFAULT TRUE,
    created_at      TIMESTAMP(3)          DEFAULT CURRENT_TIMESTAMP(3),
    updated_at      TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    deleted_at      TIMESTAMP(3),
    CONSTRAINT fk_prescription_consultation FOREIGN KEY (consultation_id) REFERENCES hubble_db.medical_consultations (id)
);

CREATE TABLE hubble_db.payment_cards
(
    id                  UUID PRIMARY KEY      DEFAULT uuid_generate_v4(),
    user_id             UUID         NOT NULL,
    provider            VARCHAR(50)  NOT NULL,
    provider_card_token VARCHAR(255) NOT NULL,
    brand               VARCHAR(30)  NOT NULL,
    last_four           VARCHAR(4)   NOT NULL,
    exp_month           SMALLINT     NOT NULL,
    exp_year            SMALLINT     NOT NULL,
    holder_name         VARCHAR(100) NOT NULL,
    is_default          BOOLEAN               DEFAULT FALSE,
    active              BOOLEAN               DEFAULT TRUE,
    created_at          TIMESTAMP(3)          DEFAULT CURRENT_TIMESTAMP(3),
    updated_at          TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    deleted_at          TIMESTAMP(3),
    CONSTRAINT fk_payment_cards_user FOREIGN KEY (user_id) REFERENCES hubble_db.users (id),
    CONSTRAINT chk_payment_cards_exp_month CHECK (exp_month BETWEEN 1 AND 12),
    CONSTRAINT chk_payment_cards_exp_year CHECK (exp_year BETWEEN 2000 AND 2100)
);

CREATE TABLE hubble_db.payments
(
    id              UUID PRIMARY KEY        DEFAULT uuid_generate_v4(),
    user_id         UUID           NOT NULL,
    appointment_id  UUID,
    exam_id         UUID,
    prescription_id UUID,
    payment_card_id UUID,
    amount          NUMERIC(10, 2)          DEFAULT 0,
    method          payment_method NOT NULL,
    status          payment_status          DEFAULT 'PENDING',
    active          BOOLEAN                 DEFAULT TRUE,
    created_at      TIMESTAMP(3)            DEFAULT CURRENT_TIMESTAMP(3),
    updated_at      TIMESTAMP(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    deleted_at      TIMESTAMP(3),
    CONSTRAINT fk_payment_user FOREIGN KEY (user_id) REFERENCES hubble_db.users (id),
    CONSTRAINT fk_payment_appointment FOREIGN KEY (appointment_id) REFERENCES hubble_db.appointments (id),
    CONSTRAINT fk_payment_exam FOREIGN KEY (exam_id) REFERENCES hubble_db.exams (id),
    CONSTRAINT fk_payment_prescription FOREIGN KEY (prescription_id) REFERENCES hubble_db.prescriptions (id),
    CONSTRAINT fk_payment_payment_card FOREIGN KEY (payment_card_id) REFERENCES hubble_db.payment_cards (id)
);

CREATE TABLE hubble_db.notifications
(
    id             UUID PRIMARY KEY           DEFAULT uuid_generate_v4(),
    user_id        UUID              NOT NULL,
    type           notification_type NOT NULL,
    title          VARCHAR(200)      NOT NULL,
    body           TEXT              NOT NULL,
    appointment_id UUID,
    payment_id     UUID,
    email_sent_at  TIMESTAMP(3),
    app_sent_at    TIMESTAMP(3),
    active         BOOLEAN                    DEFAULT TRUE,
    created_at     TIMESTAMP(3)               DEFAULT CURRENT_TIMESTAMP(3),
    updated_at     TIMESTAMP(3)      NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    deleted_at     TIMESTAMP(3),
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES hubble_db.users (id),
    CONSTRAINT fk_notifications_appointment FOREIGN KEY (appointment_id) REFERENCES hubble_db.appointments (id) ON DELETE SET NULL,
    CONSTRAINT fk_notifications_payment FOREIGN KEY (payment_id) REFERENCES hubble_db.payments (id) ON DELETE SET NULL
);

CREATE INDEX idx_users_role ON hubble_db.users (role);
CREATE INDEX idx_users_document ON hubble_db.users (document_number);
CREATE INDEX idx_appointment_user ON hubble_db.appointments (user_id);
CREATE INDEX idx_appointment_doctor ON hubble_db.appointments (doctor_id);
CREATE INDEX idx_appointment_date ON hubble_db.appointments (appointment_date);
CREATE INDEX idx_payment_cards_user ON hubble_db.payment_cards (user_id);
CREATE INDEX idx_payments_user ON hubble_db.payments (user_id);
CREATE INDEX idx_payments_payment_card ON hubble_db.payments (payment_card_id);
CREATE INDEX idx_notifications_user_created ON hubble_db.notifications (user_id, created_at);
CREATE INDEX idx_notifications_type ON hubble_db.notifications (type);
