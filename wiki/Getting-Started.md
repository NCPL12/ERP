# Getting started

## Prerequisites

- JDK 8. The Maven project declares Java 1.8 and CI builds with Temurin 8.
- Maven. The repository has a `pom.xml`; CI runs `mvn clean verify`.
- A database and configuration appropriate to the profile you select. The normal `dev`, `qa`, and `prod` profiles use MySQL. The `h2` profile uses an in-memory database and is suitable only for isolated development or testing.

## Build and run

From the repository root (`C:\Users\User\Desktop\ERP\ERP` in this checkout):

```powershell
mvn clean verify
mvn spring-boot:run -Dspring-boot.run.profiles=h2
```

The `h2` profile sets port `8880` and context path `/ncpl-sales`, so the login page is at `http://localhost:8880/ncpl-sales/login`. The in-memory database starts empty and is configured with `create-drop`; data is discarded when the process stops. An existing user account and relevant role permissions are needed to use protected pages. Do not treat the `h2` profile as a populated demo environment.

The Maven `dev` profile is active by default and filters `spring.profiles.active` in `application.properties`. To run against a configured development database, use the project's `dev` settings or pass a Spring profile explicitly. Review `application-dev.properties` locally before starting: it contains machine-specific values and is currently modified in this checkout.

To package the WAR without launching the application:

```powershell
mvn clean package
```

The application entry point is `com.ncpl.sales.SalesApp`. It also extends `SpringBootServletInitializer` for WAR deployment.

## First orientation

1. Open the login page for the selected profile and sign in with an account provisioned for that environment.
2. Use the dashboard and left menu to find the modules allowed by your role.
3. See [User workflows](User-Workflows.md) for page routes and common tasks.

If startup fails, check the selected Spring profile, database URL and credentials, database availability, and port. Keep credentials outside wiki pages and issue reports.
