# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project overview

`icn-api-dynamic-queries` is a Spring Boot 2.7.18 REST API packaged as a **WAR for deployment on IBM WebSphere** (external application server). It targets **Java 1.8**. Code comments and API messages are written in Spanish; keep that convention.

## Commands

There is no Maven wrapper (`.mvn/` is empty), so use a locally installed `mvn`. The default JDK on this machine may be newer than 8; to verify Java 8 compatibility, prefix commands with `JAVA_HOME=~/Library/Java/JavaVirtualMachines/corretto-1.8.0_372/Contents/Home`.

```bash
mvn clean package                 # build target/icn-api-dynamic-queries-1.0.0.war
mvn test                          # JUnit 5 + Mockito unit tests and @WebMvcTest slices (no DB needed)
mvn test -Dtest=ClassName#method  # run a single test
ICN_DB_PASSWORD=... mvn spring-boot:run -Dspring-boot.run.profiles=local   # run against local Oracle XE
```

Smoke-test endpoint: `GET /api/v1/ping` (under WebSphere, prefixed by the WAR's context root).

## Architecture and deployment constraints

- `ServletInitializer` is both the `@SpringBootApplication` class and the `SpringBootServletInitializer` that WebSphere uses to boot the app; its `main` exists only for standalone runs. New components must live under the `co.com.portalup` package to be picked up by component scanning.
- The `pom.xml` exclusions are deliberate to avoid classloader conflicts with WebSphere — don't remove them without a reason:
  - `spring-boot-starter-logging` is excluded (WebSphere provides logging; Spring falls back to JUL/commons-logging).
  - Embedded Tomcat and `tomcat-embed-el` are `provided` scope, so they are not bundled in the WAR.
  - `xml-apis`, `xercesImpl`, `xalan` are excluded because the server supplies the XML stack.
- Any new dependency must be Java 8–compatible and, when it pulls in logging or XML libraries, should get the same exclusions.
- `ojdbc8` is `provided`: on WebSphere the driver comes with the JNDI datasource.
- WebSphere 9 ships its own JPA 2.1 / Bean Validation 1.1; the WAR bundles Hibernate 5.6 (JPA 2.2) and Hibernate Validator 6.2, so the application classloader must be `PARENT_LAST`.

## Layering and persistence

- Plain layered structure under `co.com.portalup`: `controller` → `service` → `repository` (Spring Data JPA) → Oracle 19c, with `entity`, `dto` and `exception` packages. Controllers return DTOs, never entities.
- `exception/GlobalExceptionHandler` is the single `@RestControllerAdvice`; it extends `ResponseEntityExceptionHandler` so every error (including Spring MVC's own) uses `dto/ErrorResponse`. Add new exception mappings there.
- Logging uses commons-logging (`LogFactory`), since no SLF4J binding is bundled.
- Datasource: `application.properties` uses JNDI `jdbc/icnDS` for every profile except `local`; `application-local.properties` connects to `localhost:1521/XEPDB1` and reads the password from `ICN_DB_PASSWORD`.
- `ddl-auto=none`: the schema is owned by the DBA, never by the app. Identity columns (`GENERATED ALWAYS AS IDENTITY`) map with `GenerationType.IDENTITY`; DB-defaulted columns like `SYSTIMESTAMP` use `insertable/updatable = false` plus `@Generated(GenerationTime.INSERT)`.
