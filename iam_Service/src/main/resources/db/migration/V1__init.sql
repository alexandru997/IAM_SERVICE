CREATE TABLE posts(
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255),
    content TEXT,
    created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    likes INTEGER NOT NULL DEFAULT 0,
    Unique(title)
);

INSERT INTO posts(title, content, created, likes) VALUES (
    'First post',
    'This is the first post',
    CURRENT_TIMESTAMP,
    9
),
(
    'Second post',
    'This is the second post',
    CURRENT_TIMESTAMP,
    20
);

