<p align="center">
  <img src="logo-readme.svg" alt="fundrays" width="340">
</p>

<p align="center">
  <a href="https://github.com/workaround-org/fundrays/actions/workflows/native-image.yml">
    <img src="https://github.com/workaround-org/fundrays/actions/workflows/native-image.yml/badge.svg" alt="Native Image CI">
  </a>
  <a href="https://github.com/workaround-org/fundrays/pkgs/container/fundrays">
    <img src="https://img.shields.io/badge/container-ghcr.io-ed2a91?logo=docker&logoColor=white" alt="Container">
  </a>
  <img src="https://img.shields.io/badge/Quarkus-3.35.4-4695EB?logo=quarkus&logoColor=white" alt="Quarkus">
  <img src="https://img.shields.io/badge/Java-25-ed2a91?logo=openjdk&logoColor=white" alt="Java 25">
  <img src="https://img.shields.io/badge/native-GraalVM-ed2a91" alt="GraalVM native">
  <img src="https://img.shields.io/github/license/workaround-org/fundrays?color=ed2a91" alt="MIT License">
</p>

<p align="center">A lightweight donation management tool — create campaigns with goal amounts, share them via QR code and link, and track contributions in real time.</p>

---

## Tech stack

- **Quarkus 3.35.4** (Java 25, native image via GraalVM)
- **quarkus-renarde** — server-side MVC admin UI (Qute templates)
- **quarkus-hibernate-orm-panache** + **quarkus-jdbc-postgresql** — persistence
- **quarkus-security-jpa** — form-based admin authentication (BCrypt)
- **quarkus-rest** + **quarkus-rest-jackson** — typesafe REST API
- **quarkus-smallrye-openapi** — OpenAPI / Swagger UI
- **quarkus-mailer** — donor confirmation + admin notification mails (SMTP, Qute mail templates)

## Running in dev mode

Requires Docker (Quarkus Dev Services starts a PostgreSQL container automatically).

```bash
./mvnw quarkus:dev
```

- Admin UI: http://localhost:8080/admin  
- Swagger UI: http://localhost:8080/q/swagger-ui  
- Dev credentials: `admin` / `admin123`

## Running tests

```bash
./mvnw test
```

## Building a native image

```bash
./mvnw package -Pnative -Dquarkus.native.container-build=true
```

The resulting binary is at `target/*-runner`. The Docker image is built from `src/main/docker/Dockerfile.native`.

## REST API

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/campaigns` | List all campaigns |
| `GET` | `/api/campaigns/{slug}` | Get a single campaign |
| `POST` | `/api/campaigns` | Create a campaign (admin) |
| `PATCH` | `/api/campaigns/{slug}` | Update a campaign (admin) |
| `GET` | `/api/donations` | List donations (admin) |
| `POST` | `/api/donations/{slug}` | Record a donation |

## CI / CD

Every push to `main` builds a native image and pushes it to `ghcr.io` as `:latest`. Release tags matching `fundrays-X.Y.Z` additionally push versioned tags. Dependabot keeps Maven dependencies, Docker base images, and GitHub Actions up to date.
