# Database Convention

## Migration location
- SQL migrations are stored at: `src/main/resources/db/migration`

## Migration file naming
- Naming format: `V<version>__<description>.sql`
- Example: `V1__init_auth_tables.sql`

## Change strategy
- Keep one migration focused on one schema change set.
- Put default data seeding in a separate migration file, not mixed with schema definition.
