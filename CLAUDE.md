# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project overview

`icn-api-dynamic-queries` is a Spring Boot 2.7.18 REST API packaged as a **WAR for deployment on IBM WebSphere** (external application server). It targets **Java 1.8**. Code comments and API messages are written in Spanish; keep that convention.

## Commands

There is no Maven wrapper (`.mvn/` is empty), so use a locally installed `mvn`.

```bash
mvn clean package                 # build target/icn-api-dynamic-queries-1.0.0.war
mvn test                          # run tests (none exist yet)
mvn test -Dtest=ClassName#method  # run a single test
mvn spring-boot:run               # run locally (see caveat below)
```

Smoke-test endpoint: `GET /api/v1/ping` (under WebSphere, prefixed by the WAR's context root).

## Architecture and deployment constraints

- `ServletInitializer` is both the `@SpringBootApplication` class and the `SpringBootServletInitializer` that WebSphere uses to boot the app; its `main` exists only for standalone runs. New components must live under the `co.com.portalup` package to be picked up by component scanning.
- The `pom.xml` exclusions are deliberate to avoid classloader conflicts with WebSphere — don't remove them without a reason:
  - `spring-boot-starter-logging` is excluded (WebSphere provides logging; Spring falls back to JUL/commons-logging).
  - Embedded Tomcat and `tomcat-embed-el` are `provided` scope, so they are not bundled in the WAR.
  - `xml-apis`, `xercesImpl`, `xalan` are excluded because the server supplies the XML stack.
- Any new dependency must be Java 8–compatible and, when it pulls in logging or XML libraries, should get the same exclusions.
- Because Tomcat is `provided`, `mvn spring-boot:run` may need the provided-scope classpath to start locally; the WAR on WebSphere is the primary runtime.
- There is no `application.properties`/`application.yml` yet; Spring Boot defaults apply.
