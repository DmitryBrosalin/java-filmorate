CREATE TABLE IF NOT EXISTS mpa_rating (
	mpa_id INTEGER PRIMARY KEY,
	name VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS genres (
    genre_id INTEGER PRIMARY KEY,
	name CHARACTER VARYING(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS users (
    user_id LONG GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    login VARCHAR(50) NOT NULL,
    email VARCHAR(255) NOT NULL,
    birthday DATE NOT NULL
);

CREATE TABLE IF NOT EXISTS films (
	film_id LONG GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
	title VARCHAR(50) NOT NULL,
	description VARCHAR(200) NOT NULL,
	release_date DATE NOT NULL,
	duration INTEGER NOT NULL,
	mpa_id INTEGER REFERENCES mpa_rating (mpa_id)
);

CREATE TABLE IF NOT EXISTS likes (
	film_id LONG REFERENCES films (film_id),
	user_id LONG REFERENCES users (user_id),
	CONSTRAINT likes_pk PRIMARY KEY (film_id, user_id)
);

CREATE TABLE IF NOT EXISTS film_genres (
	film_id LONG REFERENCES films (film_id),
	genre_id INTEGER REFERENCES genres (genre_id),
	CONSTRAINT film_genres_pk PRIMARY KEY (film_id, genre_id)
);

CREATE TABLE IF NOT EXISTS friends (
	user_id LONG REFERENCES users (user_id),
	friend_id LONG REFERENCES users (user_id),
    CONSTRAINT friends_pk PRIMARY KEY (user_id, friend_id)
);