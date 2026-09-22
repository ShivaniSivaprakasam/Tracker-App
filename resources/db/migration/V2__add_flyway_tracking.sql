-- This is the first real Flyway-managed migration. It intentionally does
-- nothing to the schema (SELECT 1 is a no-op) — its purpose is simply to
-- exist as the first tracked migration going forward, now that Flyway has
-- taken over from Hibernate's ddl-auto for schema management.
SELECT 1;