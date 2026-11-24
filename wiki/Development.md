# Development

## Build and tests

The Java build uses Maven and JDK 8:

```powershell
mvn clean verify
```

Java tests are in `src/test/java`. A Node test for the PO/Tally refresh flow is in `src/test/js/po-tally-refresh.test.cjs`. The CI workflows in `.github/workflows/` run Maven verification, Java static analysis, and syntax checks on application JavaScript. The JavaScript syntax workflow uses Node 22 and checks files under `src/main/resources/static/js/`.

## Working on a feature

1. Find the page route in `controller/` or `api/`.
2. Follow the controller into `service/`, then `repository/` and `model/`.
3. Locate the JSP in `src/main/webapp/WEB-INF/views/` and its JavaScript in `src/main/resources/static/js/`.
4. Check route authorities in `security/SecurityConfig.java`, method-level authorization, and JSP visibility rules. For reports, also check `model/ReportItem.java` and role management.
5. Add a test when changing behavior that can regress, especially stock, permissions, exports, or integration logic.
6. Run `mvn clean verify`; check changed application JavaScript with `node --check` if Node is installed.

## Wiki maintenance

The Markdown in `wiki/` is the editable source for these pages. Keep routes and configuration names tied to the current code. Update the branch/date note in [Home](Home.md) when reviewing a different release. The files are named for a GitHub wiki (`Home.md` and `_Sidebar.md`); publishing them to the GitHub wiki requires access to the repository's separate wiki repository.

Avoid adding credentials, customer data, production URLs, or database dumps to documentation. Include only non-sensitive examples.
