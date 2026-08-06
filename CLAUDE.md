# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

```bash
# Build WAR (dev profile active by default)
mvn clean package

# Run embedded Tomcat (dev profile)
mvn spring-boot:run

# Run with a specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=prod

# Build for production
mvn clean package -P prod
```

The app starts at `http://localhost:8880/ncpl-sales` (dev). Port and context path are in `application-dev.properties`.

There are no automated tests (`src/test/` does not exist). Verification is done by running the app manually.

## Architecture

**Layer flow:** HTTP request → MVC Controller or REST Controller → Service → Repository → MySQL

### Packages

- `com.ncpl.sales` — main Spring Boot app (`SalesApp`)
- `com.ncpl.sales.controller` — Spring MVC `@Controller` classes (`SalesController`, `PurchaseController`, `itemMasterController`) returning Apache Tiles view names and populating `Model`/`ModelAndView`. `SalesController` is a large monolithic controller handling sales orders, delivery challans, GRNs, invoices, and more.
- `com.ncpl.sales.api` — `@RestController` classes (`SalesRestController`, `PurchaseRestController`) returning JSON, used by JSPs via AJAX. These live under `/api/**` which is permit-all in security config.
- `com.ncpl.sales.model` — JPA entities; all extend `TimeStampEntity`
- `com.ncpl.sales.service` — three distinct types (see below)
- `com.ncpl.sales.repository` — Spring Data JPA repos + one raw-JDBC repo
- `com.ncpl.sales.generator` — custom Hibernate ID generators
- `com.ncpl.sales.security` — `User`, `UserService`, `SecurityConfig`, `LoginSuccessHandler`
- `com.ncpl.sales.aspect` — `LoggingAspect` (AOP audit)
- `com.ncpl.sales.config` — Spring config beans
- `com.ncpl.sales.exception` — `GlobalExceptionHandler` (`@ControllerAdvice`, catches all exceptions, returns `ErrorResponse` with HTTP 500)
- `com.ncpl.common` — `Constants` (shared `FILE_LOCATION`, stock activity reason strings)
- `com.erp.stockreport` — `MonthlyStockReportService`: a **standalone utility** with its own `main()` that connects directly to MySQL via JDBC (not part of the Spring context); run independently to generate monthly stock Excel reports

### Views

Apache Tiles 3 with layout definitions in `src/main/webapp/WEB-INF/tiles.xml`. Every page definition wires the same header/footer/sideMenu partials from `WEB-INF/tiles/`. JSPs live in `WEB-INF/views/`. Each view has a corresponding JS file in `src/main/resources/static/js/` (e.g. `deliveryChallan.jsp` ↔ `deliveryChallan.js`). AdminLTE theme assets are in `static/dist/` and `static/plugins/`.

### Service layer

Three distinct types in `service/`:
1. **Business logic services** — `SalesService`, `PurchaseOrderService`, `InvoiceService`, etc.
2. **Excel report generators** — classes ending in `Excel` (Apache POI); some have a `*LogoService` or `*logo*` variant that adds a company logo header row.
3. **Email schedulers** — classes ending in `EmailSchedular`/`EmailScheduler` (Spring `@Scheduled`, enabled via `@EnableScheduling` on `SalesApp`); most send HTML emails using Apache Velocity templates in `src/main/resources/template/`.

### ID Generators

Every major entity uses a custom `SequenceStyleGenerator` in `generator/`. IDs are formatted strings — e.g. `SO-BLR-ELT-0001-2024` for sales orders. Check the relevant generator before assuming ID format.

### Audit Trail (two mechanisms)

1. **Hibernate Envers** — `@Audited` on all entities (via `TimeStampEntity`). Envers creates `*_AUD` shadow tables automatically.
2. **AOP Audit** — `LoggingAspect` intercepts `SalesService.savesales()`, `SalesOrderDesignService.save()`, and `PartyAddressService.savePartyAddress()`/`updatePartyAddress()`, writing `SalesOrderAudit` rows via `SalesOrderAuditService`. Do **not** add duplicate audit writes inside the service methods for these pointcuts.

### Security

`SecurityConfig` uses form login with `LoginSuccessHandler` and role-based URL authorization. Roles: `ADMIN`, `SUPER ADMIN`, `NORMAL USER`, `PURCHASE`, `STORE`, `STORE USER`, `ITEMMASTER`, `PURCHASE STORE`, `SALES`. `/api/**`, `/css/**`, `/js/**`, `/swagger-ui/**`, and `/audit/**` are permit-all. CSRF is disabled.

### Database

- Dev: local MySQL at `localhost:3306/june17`, credentials `root/Pass@123` (also accepts `MYSQL_HOST` env var)
- Prod: AWS RDS (active in `application-prod.properties`)
- Profiles available: `dev`, `prod`, `qa`, `h2`
- `spring.jpa.hibernate.ddl-auto=update` in all profiles — schema managed by Hibernate, not migrations
- `DashboardAggregateJdbcRepository` uses raw JDBC (`JdbcTemplate`) for dashboard counts via a single SQL query with scalar subqueries (performance-sensitive; add indexes on join columns if slow)

### Dashboard caching

`DashboardService` holds an in-memory snapshot (`volatile DashboardCountDto`) warmed up asynchronously on startup and refreshed every 60 seconds by `DashboardCountsRefreshScheduler` (configurable via `dashboard.counts.refresh-ms` in properties). The HTTP endpoint returns the cached snapshot; it only blocks on the very first request before warmup finishes.

### File storage

Uploaded files (design attachments, etc.) are stored on disk at `~/NCPL_FILES` (resolved via `Constants.FILE_LOCATION = SystemUtils.getUserHome() + "/NCPL_FILES"`). The `FileEntity` model and `FileRepo` track file metadata; `DesignUploadService` and `ItemMasterUploadService` handle uploads.

### Configuration classes

| Class | Purpose |
|---|---|
| `TilesConfig` | Registers `WEB-INF/tiles.xml` and Tiles view resolver |
| `AsyncConfig` | Enables `@Async` for email schedulers |
| `AuditConfig` | Registers `ObjectMapper` bean; enables AOP proxy |
| `SwaggerConfig` | Springfox 3.0.0; UI at `/swagger-ui/` |
| `VelocityConfigBean` | Points Velocity at `classpath:/template/` |
| `LangConfig` | `MessageSource` for `lang/messages_en.properties` |

## Key conventions

- All persistable entities extend `TimeStampEntity`, which auto-populates `created`, `updated`, `createdBy`, `lastModifiedBy` via JPA lifecycle hooks using the Spring Security principal.
- `util/NcplUtil.java` contains shared helpers (date formatting, number-to-words via Tradukisto, etc.). Check here before writing utility code.
- `tradukisto` converts numeric amounts to words (e.g. "Rupees One Thousand Only") for invoice printouts.
- `GlobalExceptionHandler` sets `WebDataBinder.setAutoGrowCollectionLimit(500)` globally — this is required for forms with large item lists.
- Profile-specific properties are in `application-{profile}.properties`; `application.properties` sets `spring.profiles.active=@activatedProperties@` resolved by Maven at build time.
- External integration: `tallyUrl=http://localhost:9002` (Tally accounting software) is configured in dev properties.
