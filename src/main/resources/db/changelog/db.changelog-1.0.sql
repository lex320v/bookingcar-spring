--liquibase formatted sql

--changeset lex:1
CREATE TABLE media_item
(
    id           BIGSERIAL PRIMARY KEY,
    type         VARCHAR(20) CHECK (type IN ('AVATAR', 'CAR_IMAGE', 'CAR_VIDEO')) NOT NULL,
    mime_type    VARCHAR(255)                                                     NOT NULL,
    uploader_id  BIGINT                                                           NOT NULL,
    link         VARCHAR(255)                                                     NOT NULL,
    preview_link VARCHAR(255),
    created_at   TIMESTAMP(6) DEFAULT NOW()                                       NOT NULL,
    updated_at   TIMESTAMP(6) DEFAULT NOW()                                       NOT NULL
);

--changeset lex:2
CREATE TABLE users
(
    id                   BIGSERIAL PRIMARY KEY,
    status               VARCHAR(20) CHECK (status IN ('ACTIVE', 'INACTIVE'))                    NOT NULL,
    username             VARCHAR(255) UNIQUE                                                     NOT NULL,
    password             VARCHAR(255)                                                            NOT NULL,
    firstname            VARCHAR(255)                                                            NOT NULL,
    lastname             VARCHAR(255)                                                            NOT NULL,
    birth_date           DATE                                                                    NOT NULL,
    gender               VARCHAR(20) CHECK (gender IN ('MALE', 'FEMALE'))                        NOT NULL,
    role                 VARCHAR(20) CHECK (role IN ('SUPER_ADMIN', 'ADMIN', 'OWNER', 'CLIENT')) NOT NULL,
    avatar_media_item_id BIGINT UNIQUE REFERENCES media_item (id) ON DELETE SET NULL,
    deleted_at           TIMESTAMP(6),
    created_at           TIMESTAMP(6) DEFAULT NOW()                                              NOT NULL,
    updated_at           TIMESTAMP(6) DEFAULT NOW()                                              NOT NULL
);

--changeset lex:3
ALTER TABLE media_item
    ADD CONSTRAINT media_item_uploader_id_fkey
        FOREIGN KEY (uploader_id) REFERENCES users (id) ON DELETE CASCADE;

--changeset lex:4
CREATE TABLE personal_info
(
    user_id                       BIGINT PRIMARY KEY REFERENCES users (id) ON DELETE CASCADE NOT NULL,
    driver_license_date_of_expire DATE,
    driver_license_date_of_issue  DATE,
    driver_license_code           VARCHAR(255),
    driver_license_issued_by      VARCHAR(255),
    driver_license_name           VARCHAR(255),
    driver_license_place_of_birth VARCHAR(255),
    driver_license_residence      VARCHAR(255),
    driver_license_surname        VARCHAR(255),
    driver_license_categories     VARCHAR(2) ARRAY
);

--changeset lex:5
CREATE TABLE car
(
    id           BIGSERIAL PRIMARY KEY,
    manufacturer VARCHAR(255)                                                                           NOT NULL,
    model        VARCHAR(255)                                                                           NOT NULL,
    type         VARCHAR(20) CHECK (type IN ('SEDAN', 'CROSSOVER', 'HATCHBACK', 'PICKUP', 'SPORT_CAR')) NOT NULL,
    year         INTEGER                                                                                NOT NULL,
    horsepower   INTEGER                                                                                NOT NULL,
    active       BOOLEAN                                                                                NOT NULL,
    price        INTEGER                                                                                NOT NULL,
    owner_id     BIGINT REFERENCES users (id) ON DELETE CASCADE                                         NOT NULL,
    deleted_at   TIMESTAMP(6),
    created_at   TIMESTAMP(6) DEFAULT NOW()                                                             NOT NULL,
    updated_at   TIMESTAMP(6) DEFAULT NOW()                                                             NOT NULL
);


--changeset lex:6
CREATE TABLE car_to_media_item
(
    car_id        BIGINT REFERENCES car (id) ON DELETE CASCADE        NOT NULL,
    media_item_id BIGINT REFERENCES media_item (id) ON DELETE CASCADE NOT NULL,
    position      INTEGER                                             NOT NULL,
    PRIMARY KEY (car_id, media_item_id)
);

--changeset lex:7
CREATE TABLE feedback
(
    id         BIGSERIAL PRIMARY KEY,
    rating     INTEGER                    NOT NULL,
    text       VARCHAR(255),
    created_at TIMESTAMP(6) DEFAULT NOW() NOT NULL,
    updated_at TIMESTAMP(6) DEFAULT NOW() NOT NULL
);

--changeset lex:8
CREATE TABLE request
(
    id             BIGSERIAL PRIMARY KEY,
    version        BIGINT NOT NULL,
    status         VARCHAR(20) CHECK (status IN ('OPEN', 'PROCESSING', 'CANCELED', 'REJECTED', 'FINISHED')) NOT NULL,
    comment        VARCHAR(255),
    car_id         BIGINT REFERENCES car (id) ON DELETE CASCADE                                             NOT NULL,
    client_id      BIGINT REFERENCES users (id) ON DELETE CASCADE                                           NOT NULL,
    date_time_from TIMESTAMP(6)                                                                             NOT NULL,
    date_time_to   TIMESTAMP(6)                                                                             NOT NULL,
    feedback_id    BIGINT UNIQUE REFERENCES feedback (id) ON DELETE SET NULL,
    created_at     TIMESTAMP(6) DEFAULT NOW()                                                               NOT NULL,
    updated_at     TIMESTAMP(6) DEFAULT NOW()                                                               NOT NULL
);
