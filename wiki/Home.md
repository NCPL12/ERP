# Welcome to the ERP wiki

Neptune ERP is a web application for sales, purchasing, stock, delivery, invoicing, returns, reporting, and administration. This wiki describes the local `feature/development-updates` checkout inspected on 28 October 2026, including uncommitted code. Features may differ on `main` or a deployed version.

## Start here

| If you want to… | Read |
| --- | --- |
| Run the application locally | [Getting started](Getting-Started.md) |
| Find a business task or page | [User workflows](User-Workflows.md) |
| Understand the code and data flow | [Architecture](Architecture.md) |
| Set up report and module access | [Reports and permissions](Reports-and-Permissions.md) |
| Configure a deployment or integration | [Configuration and operations](Configuration-and-Operations.md) |
| Build, test, and contribute | [Development](Development.md) |
| Diagnose a startup or access problem | [Troubleshooting](Troubleshooting.md) |

## Main workflow

```text
Parties and items → Sales order → Purchase order → Goods receipt (GRN)
                         ↓                    ↓
                  Delivery challan ← Stock movements
                         ↓
                 Invoice and returnables
```

The diagram is a navigation aid, not a required sequence for every transaction. Access to each page depends on the user's role.

## Repository map

- `src/main/java/com/ncpl/sales/controller` and `api`: web pages and JSON endpoints.
- `src/main/java/com/ncpl/sales/service`: business logic, exports, scheduled jobs, and integrations.
- `src/main/java/com/ncpl/sales/repository` and `model`: persistence.
- `src/main/java/com/ncpl/sales/security`: login and access control.
- `src/main/webapp/WEB-INF/views`: JSP pages.
- `src/main/resources/static`: application JavaScript, styles, and assets.
- `src/main/resources/application-*.properties`: environment profiles.
- `src/test`: Java and JavaScript tests.

For changes by branch, see the repository [README](https://github.com/NCPL12/ERP/blob/feature/development-updates/README.md) and [draft release notes](https://github.com/NCPL12/ERP/blob/feature/development-updates/RELEASE_NOTES.md). Those files describe a branch comparison at a point in time; check the current source before using them as deployment notes.
