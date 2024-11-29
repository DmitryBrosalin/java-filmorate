# Filmorate

## Общие фильмы

### add-common-films

Вывод общих с другом фильмов с сортировкой по их популярности.

>GET /films/common?userId={userId}&friendId={friendId}
 
Возвращает список фильмов, отсортированных по популярности.

userId — идентификатор пользователя, запрашивающего информацию;

friendId — идентификатор пользователя, с которым необходимо сравнить список фильмов.

## Удаление фильмов и пользователей

### add-remove-endpoint

Удаление фильма иили пользователя по идентификатору.

>DELETE /users/{userId}

Удаляет пользователя по идентификатору userId.

>DELETE /films/{filmId}

Удаляет фильм по идентификатору filmId.

## Добавление режиссеров в фильмы

### add-director

В информацию о фильмах добавлено имя режиссера.

Основной функционал:

1. Добавление режиссера.
>POST /directors
2. Изменение режиссера.
>PUT /directors
3. Удаление режиссера.
>DELETE /directors/{id}
4. Вывод режиссера по ID.
>GET /directors/{id}
5. Вывод список всех режиссеров.
>GET /directors
6. Вывод всех фильмов режиссера, отсортированного по годам или по количеству лайков.
>GET /films/director/{directorId}?sortBy=[year,likes]

## Добавление функции поиска

### add-search

Поиск может осуществляться по названию фильма, по режиссеру или сразу по двум критериям.

Функционал:
> GET /films/search?query=крад&by=director,title

По запросу пользователю возвращается список фильмов, удовлетворяющий указанной фразе.  Список, возвращаемый по запросу, будет получен и отфильтрован из таблицы фильмов и/или режиссеров.

## Добавление рекомендательной системы для фильмов

### add-recommendations

Пользователи смогут получить список фильмов, исходя из алгоритма подбора по схожести вкусов пользователей.

Реализована система рекомендаций по совместной фильтрации на Java, осуществляющая поиск пользователей с максимально похожими вкусами(кол-во совпадений понравившихся фильмов), и вывод пользователю рекомендаций.

Получение рекомендаций:
> GET /users/{id}/recommendations

## Отзывы

### add-reviews

В приложение добавлены отзывы на фильмы. Рейтинг имеет поля:
- reviewId
- userId
- filmId
- content (содержание, текст)
- isPositive (тип отзыва - позитивный/негативный)
- useful - рейтинг (сколько пользователей посчитали отзыв полезным)

Пользователи могут ставить лайки и дизлайки на отзывы, тем самым меняя рейтинг отзывов. При создании отзыва рейтинг равен нулю. Если пользователь оценил отзыв как полезный, это увеличивает его рейтинг на 1. Если как бесполезный, то уменьшает на 1.

При выводе отзывы отсортированы по рейтингу полезности.

>POST /reviews

Добавление нового отзыва.

>PUT /reviews

Редактирование уже имеющегося отзыва.

>DELETE /reviews/{id}

Удаление уже имеющегося отзыва.

>GET /reviews/{id}

Получение отзыва по идентификатору.

>GET /reviews?filmId={filmId}&count={count}
> 
Получение всех отзывов по идентификатору фильма, если фильм не указан то все. Если кол-во не указано, то 10.

>PUT /reviews/{id}/like/{userId}

Пользователь ставит лайк отзыву.

>PUT /reviews/{id}/dislike/{userId}

Пользователь ставит дизлайк отзыву.

>DELETE /reviews/{id}/like/{userId}

Пользователь удаляет лайк/дизлайк отзыву.

>DELETE /reviews/{id}/dislike/{userId}

Пользователь удаляет дизлайк отзыву.

## Лента событий

### add-feed

Наша платформа теперь поддерживает возможность просмотра ленты событий для каждого пользователя. Лента событий отображает историю действий, связанных с активностью пользователя и его друзей, таких как добавление в друзья, удаление из друзей, отметки "Нравится" (лайки) и отзывы.

Функционал:
1. Отслеживание событий:
    - Платформа фиксирует события трех типов:
        - LIKE (Лайк) — пользователь поставил или убрал отметку "Нравится" на контенте.
        - REVIEW (Отзыв) — пользователь или его друг написал/удалил/обновил отзыв.
        - FRIEND (Друзья) — действия пользователя с друзьями (добавление или удаление друга).
2. Лента событий:
    - Теперь пользователи могут запросить ленту действий через API:
> GET /users/{id}/feed
3. Хранение событий:
    - Добавлена специальная таблица в базе данных для хранения всех событий. Она позволяет вести журнал действий пользователя:
        - Кто взаимодействовал с объектом (user_Id).
        - Тип события (event_type (LIKE, REVIEW, FRIEND)).
        - Операция, связанная с событием (operation (ADD, REMOVE, UPDATE)).
        - Временная метка события (timestamp).
        - Идентификаторы сущности и события (entity_Id, event_Id).

## Вывод самых популярных фильмов по жанру и годам

### add-most-populars

Добавлена возможность выводить топ-N фильмов по количеству лайков.

Фильтрация реализована по двум параметрам: по жанру и за указанный год.

> GET /films/popular?count={limit}&genreId={genreId}&year={year}

Возвращает список самых популярных фильмов указанного жанра за нужный год.
