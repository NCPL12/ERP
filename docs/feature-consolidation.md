# Development feature consolidation

This integration combines `development` at `9d4e975` with the source features
available on `features/dev` at `9be7c45`, plus the saved local compatibility fixes.
Other remote feature branches are not part of this integration.

The ordinary Git merge only adds the feature branch's build-output cleanup:
its source code was already present in the common ancestor and later replaced
on development. A separate three-way source integration restores those features
while retaining development's newer fixes.

Included behavior:

- Permission-based menus, routes, login destinations, and Roles and Access.
- Paginated Returnables, creation and detail views, and the separate Returnable DC page.
- Dashboard count and list caches, asynchronous warmup, and scheduled refresh.
  Background list loading runs inside a read-only transaction. A cold dashboard
  loads immediately and refreshes once its list snapshot becomes available.
- Batch queries for sales, delivery challans, TDS, and stock/report workflows.
- Company asset handling, user management, and purchase audit integrations.
- Development's timestamp, billing-address, monthly movement, TDS batching,
  purchase-description, and single-click GRN changes.
- Generated `target/` output is no longer tracked.

Validation:

- Java 8 / Maven compile and WAR packaging passed.
- Node syntax checks passed for all 18 changed JavaScript files.
- Database-free runtime checks passed for dashboard transaction boundaries and
  cache reuse, permission changes, disabled accounts, and 282 controller mappings.
- Tiles XML parses and contains no duplicate definition names.

A live MySQL startup and browser walkthrough have not been verified.
Local `application-dev.properties` settings remain uncommitted.
The pre-integration branch is `backup/development-before-features-dev-20261014`;
the stash titled "Local changes before merging features/dev into development
2026-10-14" retains the original local work and generated files.
