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
	film_id LONG REFERENCES films (film_id) ON DELETE CASCADE,
	user_id LONG REFERENCES users (user_id) ON DELETE CASCADE,
	CONSTRAINT likes_pk PRIMARY KEY (film_id, user_id)
);

CREATE TABLE IF NOT EXISTS film_genres (
	film_id LONG REFERENCES films (film_id) ON DELETE CASCADE,
	genre_id INTEGER REFERENCES genres (genre_id),
	CONSTRAINT film_genres_pk PRIMARY KEY (film_id, genre_id)
);

CREATE TABLE IF NOT EXISTS friends (
	user_id LONG REFERENCES users (user_id) ON DELETE CASCADE,
	friend_id LONG REFERENCES users (user_id) ON DELETE CASCADE,
    CONSTRAINT friends_pk PRIMARY KEY (user_id, friend_id)
);

CREATE TABLE IF NOT EXISTS review (
    review_id SERIAL PRIMARY KEY,
    content VARCHAR(500) NOT NULL,
    is_positive BOOLEAN,
    user_id INTEGER NOT NULL,
    film_id INTEGER NOT NULL,
    useful INTEGER DEFAULT 0,
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_film FOREIGN KEY (film_id) REFERENCES films(film_id) ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS review_likes (
    review_id INT NOT NULL,
    user_id INT NOT NULL,
    like_status INT NOT NULL,
    PRIMARY KEY (review_id, user_id),
    FOREIGN KEY (review_id) REFERENCES review (review_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
);

