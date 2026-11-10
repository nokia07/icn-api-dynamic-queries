# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project overview

`icn-api-dynamic-queries` is a Spring Boot 2.7.18 REST API packaged as an **EAR for deployment on IBM WebSphere 9** (external application server). It is a Maven multi-module build, modeled on `~/projects/workspace/portalup/Prote_ConsGral/portalup-ear`:

- `pom.xml` — parent/aggregator (inherits `spring-boot-starter-parent`).
- `app-war/` — all the application code and tests; builds `icn-api-dynamic-queries.war`.
- `app-ear/` — wraps the WAR into `icn-api-dynamic-queries.ear` with context root `/icn-api-dynamic-queries`.

It targets **Java 1.8**. Code comments and API messages are written in Spanish; keep that convention.

## Commands

There is no Maven wrapper (`.mvn/` is empty), so use a locally installed `mvn`. The default JDK on this machine may be newer than 8; to verify Java 8 compatibility, prefix commands with `JAVA_HOME=~/Library/Java/JavaVirtualMachines/corretto-1.8.0_372/Contents/Home`.

```bash
mvn clean package                 # build app-ear/target/icn-api-dynamic-queries.ear (and the WAR in app-war/target)
mvn test                          # JUnit 5 + Mockito unit tests and @WebMvcTest slices (no DB needed)
mvn test -pl app-war -Dtest=ClassName#method   # run a single test
# run against local Oracle XE (from app-war/, after `mvn install -DskipTests` at the root)
ICN_DB_PASSWORD=... mvn spring-boot:run -Dspring-boot.run.profiles=local
```

Smoke-test endpoint: `GET /api/v1/ping` (under WebSphere: `/icn-api-dynamic-queries/api/v1/ping`).

## Architecture and deployment constraints

- `ServletInitializer` is both the `@SpringBootApplication` class and the `SpringBootServletInitializer` that WebSphere uses to boot the app; its `main` exists only for standalone runs. New components must live under the `co.com.portalup` package to be picked up by component scanning.
- The `app-war/pom.xml` exclusions are deliberate to avoid classloader conflicts with WebSphere — don't remove them without a reason:
  - `spring-boot-starter-logging` is excluded (WebSphere provides logging; Spring falls back to JUL/commons-logging).
  - Embedded Tomcat and `tomcat-embed-el` are `provided` scope, so they are not bundled in the WAR.
  - `xml-apis`, `xercesImpl`, `xalan` are excluded because the server supplies the XML stack.
- Any new dependency must be Java 8–compatible and, when it pulls in logging or XML libraries, should get the same exclusions.
- `ojdbc8` is `provided`: on WebSphere the driver comes with the JNDI datasource.
- WebSphere 9 ships its own JPA 2.1 / Bean Validation 1.1; the WAR bundles Hibernate 5.6 (JPA 2.2) and Hibernate Validator 6.2, so the application classloader must be `PARENT_LAST` (WAR classloader policy `SINGLE`). The reference project documents `PARENT_FIRST`; that does not apply here.
- `spring.jta.enabled=false`: with `PARENT_LAST` the bundled `javax.transaction` API clashes with WebSphere's `UserTransaction`; one datasource needs no JTA.
- The WAR build skips Spring Boot's `repackage` (WebSphere ignores its loader and `lib-provided`) and excludes `localhost.p12` and `application-local.properties`. `WEB-INF/beans.xml` uses `bean-discovery-mode="none"` so WebSphere's CDI does not scan the app.
- In WebSphere ICN and the API share host and port, so no CORS configuration is needed there; `cors.allowed-origins` is only set for the local profile.

## Security

Two layers, no Spring Security:

- **Authentication — WebSphere container security.** `app-war/src/main/webapp/WEB-INF/web.xml`: every method except `OPTIONS` on `/api/*` needs role `DQUser`; `/api/v1/ping` is open. Because ICN and the API share host and port, the browser sends the LTPA cookie (`LtpaToken2`) it got when logging into ICN, so the ICN user is already authenticated (no login of its own; without a valid cookie WebSphere answers 401 with a BASIC challenge). `DQUser` is declared in `web.xml` and in the generated `application.xml` (`maven-ear-plugin` `<security>`), and `app-ear/src/main/application/META-INF/ibm-application-bnd.xml` maps it to `ALL_AUTHENTICATED_USERS`, so nothing has to be mapped in the WAS console. Requires WebSphere application security enabled (ICN already needs it).
- **Authorization of writes — table `tbl_icn_designer_groups`** (DDL in `docs/db/tbl_icn_designer_groups.sql`, created by the DBA; `DesignerGroupEntity`, read-only). `DesignerAccessInterceptor` (registered by `DesignerAccessConfig` on `/api/**`) lets GET/HEAD/OPTIONS through and, for any other method, asks `DesignerAccessService.canDesign()`: the user must belong to an ACTIVE group (case-insensitive, short name or DN), otherwise `DesignerAccessDeniedException` → 403 `ErrorResponse`. No active groups = nobody can write. The table is read on every write (no cache). The user's groups come from `WebSphereUserGroupsProvider` (`WSSubject.getCallerSubject()` → `WSCredential.getGroupIds()`, `group:realm/uniqueId` → DN and CN), which needs `com.ibm.websphere:was-public:9.0.0.8` (`provided`, like Prote_ConsGral; it must be in the local Maven repository). The ICN plug-in doesn't read this table: its own `designerGroups` setting only shows or hides the designer UI, so keep both in line.
- `@WebMvcTest` slices load `DesignerAccessConfig`, so each controller test declares `@MockBean DesignerAccessService` and allows design in `@BeforeEach`; `QueryControllerTest` covers the 403.
- CSRF: the session is a cookie, so cross-site requests carry it. Writes are only reachable with a JSON body (`@RequestBody`, non-JSON → 415, tested in `QueryControllerTest`) or with `PUT`/`DELETE`, both of which force a CORS preflight that WebSphere doesn't allow. Keep every write endpoint taking a JSON `@RequestBody` or a non-simple method; never add a write via `GET` or a form-encoded `POST`.
- Embedded Tomcat (`spring-boot:run`, local profile) ignores `web.xml`, and `application-local.properties` sets `dq.security.designer-check=false`: locally the API is open.

## Layering and persistence

- Plain layered structure under `co.com.portalup`: `controller` → `service` → `repository` (Spring Data JPA) → Oracle 19c, with `entity`, `dto` and `exception` packages. Controllers return DTOs, never entities.
- `exception/GlobalExceptionHandler` is the single `@RestControllerAdvice`; it extends `ResponseEntityExceptionHandler` so every error (including Spring MVC's own) uses `dto/ErrorResponse`. Add new exception mappings there.
- Logging uses commons-logging (`LogFactory`), since no SLF4J binding is bundled.
- Datasource: `application.properties` uses JNDI `jdbc/GeneraConsDinaDS` for every profile except `local`; `application-local.properties` connects to `localhost:1521/XEPDB1` and reads the password from `ICN_DB_PASSWORD`.
- `ddl-auto=none`: the schema is owned by the DBA, never by the app. Identity columns (`GENERATED ALWAYS AS IDENTITY`) map with `GenerationType.IDENTITY`; DB-defaulted columns like `SYSTIMESTAMP` use `insertable/updatable = false` plus `@Generated(GenerationTime.INSERT)`.
