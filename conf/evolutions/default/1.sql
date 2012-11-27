# --- !Ups

CREATE TABLE users (
    user_id              BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    email                VARCHAR(255) NOT NULL UNIQUE,
    password_hash        VARCHAR(255) NOT NULL,
    first_name           VARCHAR(255) NOT NULL,
    last_name            VARCHAR(255) NOT NULL,
    is_active            BOOLEAN NOT NULL DEFAULT 0,
    last_modified        TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE rights (
    right_id             BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name                 VARCHAR(255) NOT NULL UNIQUE,
    last_modified        TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_right_map (
    id                   BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id              BIGINT NOT NULL REFERENCES users (user_id)
                           ON DELETE CASCADE ON UPDATE RESTRICT,
    right_id              BIGINT NOT NULL REFERENCES rights (right_id)
                           ON DELETE CASCADE ON UPDATE RESTRICT,
    created              TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, right_id)
);


# --- !Downs

DROP TABLE IF EXISTS user_right_map;

DROP TABLE IF EXISTS rights;

DROP TABLE IF EXISTS users;
