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
    user_id INTEGER NOT NULL ,
    title   VARCHAR(255),
    content TEXT,
    created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN   NOT NULL DEFAULT FALSE,
    likes   INTEGER   NOT NULL DEFAULT 0,
    created_by VARCHAR(50),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,

    Unique (title)
);


INSERT INTO users(username, password, email,created, updated, registration_status, last_login, deleted) VALUES
                  ('first_user', '$2a$10$6zddZfS84WBX/tM5flqq6e/ves0CzUcYVcW33LpWzakiwZXEx/1By', 'first_user@gmail.com', CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,'ACTIVE', CURRENT_TIMESTAMP, false),
                  ('second_user', '$2a$10$TumEkYb6AvGqHpvflKenLeD66UodoMU8Et11.lCGHwjjLU.CvY6ku', 'second_user@gmail.com', CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,'ACTIVE', CURRENT_TIMESTAMP, false),
                  ('third_user', '$2a$10$3UC6FbKbGACAAPDkqMgiN.n2MSygUyHb6vDCjCVbkNLMIjduG9Nbe', 'third_user_user@gmail.com', CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,'ACTIVE', CURRENT_TIMESTAMP, false)

;
INSERT INTO posts (user_id, title, content, created, updated, deleted, likes) VALUES
                                                                                  (1, 'First Post', 'This is content of the first post', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false, 6),
                                                                                  (2, 'Second Post', 'This is content of the second post', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false, 3);
-- INSERT INTO posts(title, content, created, updated, deleted, likes)
-- VALUES ('First post',
--         'This is the first post',
--         CURRENT_TIMESTAMP,
--         CURRENT_TIMESTAMP,
--         false,
--         9),
--        ('Second post',
--         'This is the second post',
--         CURRENT_TIMESTAMP,
--         CURRENT_TIMESTAMP,
--         false,
--         20);

