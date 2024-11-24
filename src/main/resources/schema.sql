CREATE TABLE IF NOT EXISTS mpa_rating (
    mpa_id INTEGER PRIMARY KEY,
    name VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS genres (
    genre_id INTEGER PRIMARY KEY,
    name CHARACTER VARYING(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS users (
    user_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    login VARCHAR(50) NOT NULL,
    email VARCHAR(255) NOT NULL,
    birthday DATE NOT NULL
);

CREATE TABLE IF NOT EXISTS films (
    film_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title VARCHAR(50) NOT NULL,
    description VARCHAR(200) NOT NULL,
    release_date DATE NOT NULL,
    duration INTEGER NOT NULL,
    mpa_id INTEGER REFERENCES mpa_rating (mpa_id)
);

CREATE TABLE IF NOT EXISTS likes (
    film_id BIGINT REFERENCES films (film_id) ON DELETE CASCADE,
    user_id BIGINT REFERENCES users (user_id) ON DELETE CASCADE,
    PRIMARY KEY (film_id, user_id)
);

CREATE TABLE IF NOT EXISTS film_genres (
    film_id BIGINT REFERENCES films (film_id) ON DELETE CASCADE,
    genre_id INTEGER REFERENCES genres (genre_id),
    PRIMARY KEY (film_id, genre_id)
);

CREATE TABLE IF NOT EXISTS friends (
    user_id BIGINT REFERENCES users (user_id) ON DELETE CASCADE,
    friend_id BIGINT REFERENCES users (user_id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, friend_id)
);

CREATE TYPE IF NOT EXISTS event_type AS ENUM (
    'LIKE',
    'REVIEW',
    'FRIEND'
);

CREATE TYPE IF NOT EXISTS operation_type AS ENUM (
    'REMOVE',
    'ADD',
    'UPDATE'
);

CREATE TABLE IF NOT EXISTS feed (
    event_id    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    timestamp   TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    user_id     BIGINT REFERENCES users (user_id) ON DELETE CASCADE,
    event_type  event_type NOT NULL,
    operation   operation_type NOT NULL,
    entity_id   BIGINT NOT NULL
);

CREATE INDEX idx_feed_user_id ON feed (user_id);


