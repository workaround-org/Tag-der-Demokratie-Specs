# CLAUDE.md – fundrays

## Tech Stack

- **Quarkus** 3.35.4, Java 25
- **quarkus-rest** + **quarkus-rest-jackson** – Jakarta REST endpoints with Jackson JSON
- **quarkus-smallrye-openapi** – OpenAPI / Swagger UI
- **quarkus-hibernate-orm** + **quarkus-hibernate-orm-panache** – ORM (use repository pattern, see below)
- **quarkus-jdbc-postgresql** – PostgreSQL
- **quarkus-security-jpa** – JPA-backed authentication/authorization
- **quarkus-renarde** 3.1.7 – MVC web framework (server-side rendering)
- **quarkus-smallrye-health** – Liveness/readiness health checks
- **quarkus-arc** – CDI dependency injection

Testing: **quarkus-junit** + **rest-assured**

## Coding Conventions

### Package structure
All classes must be under `de.fundrays.*`. Sub-packages: `model`, `rest`, `util`, etc.

### Data access
Always use the **repository pattern** (`PanacheRepository<T>`). Never call persistence methods directly on entities (no active record pattern).

```java
// correct
@ApplicationScoped
public class TodoRepository implements PanacheRepository<Todo> { }

// wrong – do not do this
Todo.listAll();
todo.persist();
```

### Tests
All unit and integration tests must use `// given` / `// when` / `// then` section comments.

```java
@Test
void shouldAddTodo() {
    // given
    var todo = new Todo();
    todo.task = "Buy milk";

    // when
    repository.persist(todo);

    // then
    assertThat(repository.count()).isEqualTo(1);
}
```
