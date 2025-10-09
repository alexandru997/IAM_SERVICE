CREATE TABLE users(
    id                  BIGSERIAL PRIMARY KEY,
    username            VARCHAR(255) UNIQUE NOT NULL,
    password            VARCHAR(255)        NOT NULL,
    email               VARCHAR(255) UNIQUE NOT NULL,
    created             TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated             TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP,
    registration_status VARCHAR(30)         NOT NULL,
    last_login          TIMESTAMP,
    deleted             BOOLEAN             NOT NULL DEFAULT FALSE

);


CREATE TABLE posts
(
    id      BIGSERIAL PRIMARY KEY,
    title   VARCHAR(255),
    content TEXT,
    created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN   NOT NULL DEFAULT FALSE,
    likes   INTEGER   NOT NULL DEFAULT 0,
    Unique (title)
);


INSERT INTO users(username, password, email,created, updated, registration_status, last_login, deleted) VALUES
                  ('first_user', 'password', 'first_user@gmail.com', CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,'ACTIVE', CURRENT_TIMESTAMP, false),
                  ('second_user', 'password1', 'second_user@gmail.com', CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,'ACTIVE', CURRENT_TIMESTAMP, false),
                  ('third_user', 'password2', 'third_user_user@gmail.com', CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,'ACTIVE', CURRENT_TIMESTAMP, false)

;

INSERT INTO posts(title, content, created, updated, deleted, likes)
VALUES ('First post',
        'This is the first post',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        false,
        9),
       ('Second post',
        'This is the second post',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        false,
        20);

