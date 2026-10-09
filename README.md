# Humanit Client API

Humanit is a Spring Boot REST API for managing clients and the documents associated with them. It supports creating, listing, retrieving, updating, and deleting clients. 

## Run the application

From the project root, run:

```bash
./gradlew bootRun
```

The server listens on `http://localhost:8080` by default.

Account registration and sign-in are public. All client endpoints require a JWT bearer token in the `Authorization` header.

### Run with Docker

Install Docker with Docker Compose, then run from the project root:

```bash
docker compose up --build
```

Compose builds the application image from the `Dockerfile` and starts the API on port `8080`. Open <http://localhost:8080> when the container is running. To run it in the background, use `docker compose up --build -d`; view its output with `docker compose logs -f humanit`, and stop and remove the container with `docker compose down`.

The database is in-memory, so its contents are reset when the container is stopped and started again. The image build runs the Gradle `bootJar` task inside a Java 25 build image; a local Gradle build is not required.

## API endpoints

Register with `POST /auth/register` using an email and a password of at least 12 characters. Then call `POST /auth/login` with the same credentials to receive an access token:

```json
{"email":"person@example.com","password":"secure-password-123"}
```

Use the returned token on client requests as `Authorization: Bearer <accessToken>`. Tokens expire after one hour by default.

The interactive API documentation is available without a bearer token for development. The H2 console, like client endpoints, requires a bearer token.

All client endpoints are under `/clients`.

| Method | Path | Purpose | Success response |
| --- | --- | --- | --- |
| `GET` | `/clients` | List all clients | `200 OK` |
| `GET` | `/clients/{id}` | Get a client by ID | `200 OK`; `404 Not Found` if missing |
| `POST` | `/clients` | Create a client | `201 Created` with the new client |
| `PUT` | `/clients/{id}` | Replace a client's fields and documents | `200 OK`; `404 Not Found` if missing |
| `DELETE` | `/clients/{id}` | Delete a client | `204 No Content`; `404 Not Found` if missing |

```bash
curl -H "Authorization: Bearer <accessToken>" http://localhost:8080/clients
```

## Swagger / OpenAPI

Open the interactive Swagger UI at:

<http://localhost:8080/swagger-ui/index.html>

Swagger UI lets you inspect the available operations, view their request and response formats, and try API calls.

The OpenAPI definition is available at <http://localhost:8080/v3/api-docs>.

## H2 database console

The H2 web console is enabled at:

<http://localhost:8080/h2-console>

It is a developer tool for connecting to the application's database, running SQL, and viewing or editing tables and data. Use these JDBC connection settings:

| Setting | Value |
| --- | --- |
| JDBC URL | `jdbc:h2:mem:clientsdb` |
| User name | `sa` |
| Password | *(blank)* |

The H2 console is for local development only.

## Database lifecycle and scripts

The application uses an in-memory H2 database. It is created when the application starts and is lost when the application shuts down; it is not a persistent database.

Database initialization is configured in `src/main/resources/application.properties`. SQL initialization runs at startup, and Hibernate schema generation is disabled:

- `src/main/resources/schema.sql` creates the `client` and `document` tables, including their primary key and foreign key relationship.
- `src/main/resources/data.sql` inserts example clients and documents.

These scripts run on every application startup, so the in-memory database starts with the schema and sample data each time. Changes made through the API or H2 console exist only for the current application run.
