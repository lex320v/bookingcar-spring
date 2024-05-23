--liquibase formatted sql

--changeset lex:1
INSERT INTO "users"("status",
                    "username",
                    "password",
                    "firstname",
                    "lastname",
                    "birth_date",
                    "gender",
                    "role",
                    "avatar_media_item_id",
                    "deleted_at",
                    "created_at",
                    "updated_at")
VALUES ('ACTIVE',
        'superadmin',
        '{noop}qwerty',
        'суперадмин',
        'суперадмин',
        '2000-01-01'::date,
        'MALE',
        'SUPER_ADMIN',
        NULL,
        NULL,
        now(),
        now());
