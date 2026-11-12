# CineMatch API (Assignment 2 Prototype)

REST API for the CineMatch project (Group 12). Java 17 + Javalin.

There is **no database**: movies are read from the TMDB 5000 CSV file (`src/main/resources/data/tmdb_5000_movies.csv`) when the server starts, and ratings are mock data kept in memory.

> Mock data (ratings) resets every time the server restarts.

## Requirements

Before installing and running the project, make sure you have:

- **JDK 17 or newer**
- **Apache Maven**
- **Git**

You can verify the installations using:

```bash
java -version
mvn -version
git --version
```

## Installation and Setup

### 1. Clone the Repository

```bash
git clone https://github.com/kabbasova20408/intro_to_software_enginering_group12.git
cd intro_to_software_enginering_group12
```

### 2. Check the Dataset

Make sure the TMDB 5000 dataset is available at:

```text
src/main/resources/data/tmdb_5000_movies.csv
```

The application requires this file to load movie information.

### 3. Build and Run the Application

From the project root directory, execute:

```bash
mvn clean compile exec:java
```

Maven automatically downloads the required dependencies, compiles the project, and starts the application.

When the server starts, open:

http://localhost:8080/api/v1/greeting

The API runs on port `8080`.

To stop the server, press `Ctrl + C` in the terminal.

## Mock Users

| Role | Token | Permissions |
|---|---|---|
| Regular user A | `user-a-token` | Manages only own ratings |
| Regular user B | `user-b-token` | Manages only own ratings |
| Administrator | `admin-token` | Manages all ratings |

Send the token like this:

```http
Authorization: Bearer user-a-token
```

## Endpoints

Main resource: **ratings** (a 1-5 score a user gives to a movie). `movies` is a public read-only catalog.

| Method | Path | Token | Success |
|---|---|---|---|
| GET | `/api/v1/greeting` | no (HTML page) | 200 |
| GET | `/health` | no | 200 |
| GET | `/api/v1/movies?search=&genre=&year=&page=&perPage=` | no | 200 |
| GET | `/api/v1/movies/{id}` | no | 200 |
| GET | `/api/v1/ratings?page=&perPage=` | yes | 200 |
| GET | `/api/v1/ratings/{id}` | yes | 200 |
| POST | `/api/v1/ratings` | yes | 201 |
| PUT | `/api/v1/ratings/{id}` | yes | 200 |
| DELETE | `/api/v1/ratings/{id}` | yes | 200 (`data` is `null`) |

Rating body:

```json
{
  "movieId": 19995,
  "score": 5,
  "comment": "optional, max 500 characters"
}
```

### Validation Rules

- `movieId`: required, integer, must be a movie that exists in the data
- `score`: required, integer from 1 to 5
- `comment`: optional text, up to 500 characters
- unknown fields are rejected
- one user can rate a movie only once (409)
- `page` is 1 or more, `perPage` is 1 to 100

## Responses

Success:

```json
{
  "success": true,
  "message": "...",
  "data": {}
}
```

List responses also have `meta` with `total`, `page`, and `perPage`.

Error:

```json
{
  "success": false,
  "error": {
    "code": "...",
    "message": "...",
    "details": []
  }
}
```

`details` appears only for validation errors.

| Status | Code | When |
|---|---|---|
| 400 | `VALIDATION_ERROR` | Invalid or missing fields, wrong types, unknown fields, bad query parameters |
| 400 | `BAD_REQUEST` | Body is empty or not valid JSON |
| 401 | `UNAUTHORIZED` | Token is missing or invalid |
| 404 | `NOT_FOUND` | ID does not exist, rating belongs to another user, or unknown endpoint |
| 405 | `METHOD_NOT_ALLOWED` | HTTP method not supported for the endpoint |
| 409 | `CONFLICT` | This user already rated this movie |
| 500 | `INTERNAL_ERROR` | Unexpected error (no internal details are shown) |

**Why 404 and not 403:** If User A asks for User B's rating, the API answers `404 Not Found`. This way it does not reveal that the rating exists.

## Example Requests (curl)

```bash
# HTML greeting page
curl -i http://localhost:8080/api/v1/greeting

# Movies (public)
curl "http://localhost:8080/api/v1/movies?search=avatar&year=2009"
curl http://localhost:8080/api/v1/movies/19995

# List own ratings
curl -H "Authorization: Bearer user-a-token" http://localhost:8080/api/v1/ratings

# Create a rating (201)
curl -X POST http://localhost:8080/api/v1/ratings \
  -H "Authorization: Bearer user-a-token" -H "Content-Type: application/json" \
  -d '{"movieId": 550, "score": 5, "comment": "Great twist"}'

# Update a rating (200)
curl -X PUT http://localhost:8080/api/v1/ratings/rating_101 \
  -H "Authorization: Bearer user-a-token" -H "Content-Type: application/json" \
  -d '{"movieId": 19995, "score": 4, "comment": "Updated"}'

# Delete a rating (200)
curl -X DELETE -H "Authorization: Bearer user-a-token" http://localhost:8080/api/v1/ratings/rating_101

# 401: no token, then wrong token
curl -i http://localhost:8080/api/v1/ratings
curl -i -H "Authorization: Bearer wrong" http://localhost:8080/api/v1/ratings

# 404: user A tries to read user B's rating
curl -i -H "Authorization: Bearer user-a-token" http://localhost:8080/api/v1/ratings/rating_103

# Admin can read it
curl -H "Authorization: Bearer admin-token" http://localhost:8080/api/v1/ratings/rating_103

# 404: ID that does not exist
curl -i -H "Authorization: Bearer admin-token" http://localhost:8080/api/v1/ratings/rating_999

# 400: validation error (score out of range + unknown field)
curl -i -X POST http://localhost:8080/api/v1/ratings \
  -H "Authorization: Bearer user-a-token" -H "Content-Type: application/json" \
  -d '{"movieId": 550, "score": 9, "extra": true}'

# 409: user A already rated Avatar
curl -i -X POST http://localhost:8080/api/v1/ratings \
  -H "Authorization: Bearer user-a-token" -H "Content-Type: application/json" \
  -d '{"movieId": 19995, "score": 3}'

# 405: PATCH is not supported
curl -i -X PATCH -H "Authorization: Bearer user-a-token" http://localhost:8080/api/v1/ratings
```

Seed data: `rating_101` and `rating_102` belong to User A, `rating_103` and `rating_104` to User B.

## Code Overview

| File | What it does |
|---|---|
| `Main.java` | Starts the server, connects routes, error handling |
| `Auth.java` | Checks the token (mock users) |
| `RatingRoutes.java` | Ratings CRUD, validation, ownership rule |
| `MovieRoutes.java` | Movie list and movie details |
| `Movies.java` | Reads the CSV file into memory |
| `Greeting.java` | The HTML greeting page |
| `Respond.java`, `ApiException.java`, `Paging.java` | Response format, errors, pagination |

## Contributors

See [`CONTRIBUTORS.md`](CONTRIBUTORS.md) for team members, student IDs, and individual responsibilities.

## Git Contribution Summary

The following command displays the number of commits made by each contributor:

```bash
git shortlog -sn --all
```

Paste the actual output below before submission:

```text
TODO: Add the output of git shortlog -sn --all
```

## Demo Video

**Video link:** TODO