# Architecture

Neptune ERP is a Java 8, Spring Boot 2.2.2 application packaged as a WAR. It uses Spring MVC and JSP/Tiles for server-rendered pages, Spring Data JPA with MySQL for normal environments, and Spring Security for login and authorization. H2 is available for an isolated test profile.

```text
Browser (JSP + JavaScript)
       ↓
Spring MVC controllers / JSON API
       ↓
Services (transactions, reports, exports, scheduled jobs)
       ↓
Spring Data repositories → MySQL or H2
```

## Code layout

| Location | Responsibility |
| --- | --- |
| `SalesApp.java` | Spring Boot entry point and servlet initializer; enables scheduling. |
| `controller/` and `api/` | Page routing and JSON endpoints. `SalesController` and `PurchaseController` cover many workflows. |
| `service/` | Business operations, stock movement, reports, spreadsheet/PDF export, email jobs, and Tally sync. |
| `repository/` and `model/` | JPA repositories and domain entities. |
| `security/` | Authentication, role checks, and active-session user status filtering. |
| `config/` | Application configuration, permission seeding, and error alerts. |
| `src/main/webapp/WEB-INF/views/` | JSP views; `WEB-INF/tiles.xml` defines layouts. |
| `src/main/resources/static/` | Browser scripts, styles, and assets. |

The app exposes JSON endpoints under several `/api/...` paths in addition to page routes. Springfox is included for Swagger/OpenAPI UI, but endpoint availability depends on the running profile and security configuration.

## Important cross-cutting behavior

- `SecurityConfig` assigns route-level authorities and requires authentication for other routes. Some methods also use method-level security.
- Module permissions, dashboard tile visibility, and report selection are configured for each role. Report routes have individual authority checks.
- Background schedulers send reports and refresh dashboard counts.
- Purchase orders can be exported or synchronized with Tally; the destination company is validated before an export starts.
- The `dev` and `prod` configurations can send asynchronous email alerts for HTTP 5xx responses, subject to a repeat cooldown.

The app has a large controller and service surface. When changing a workflow, trace its page route, JavaScript, controller, service, repository, and permission checks together.
