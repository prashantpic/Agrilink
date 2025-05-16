import { schemaMigrations, createTable, addColumns } from '@nozbe/watermelondb/Schema/migrations';
import { localAppSchema } from '../schema'; // Assuming this exports the latest schema version

// WatermelonDB migrations documentation: https://nozbe.github.io/WatermelonDB/Advanced/Migrations.html

export const migrations = schemaMigrations({
  migrations: [
    // {
    //   toVersion: 2,
    //   steps: [
    //     // Example: adding a new table
    //     // createTable({
    //     //   name: 'new_entities',
    //     //   columns: [
    //     //     { name: 'name', type: 'string' },
    //     //     { name: 'created_at', type: 'number' },
    //     //     { name: 'updated_at', type: 'number' },
    //     //   ],
    //     // }),
    //     // Example: adding columns to an existing table
    //     // addColumns({
    //     //   table: 'farmers',
    //     //   columns: [
    //     //     { name: 'new_field', type: 'string', isOptional: true },
    //     //   ],
    //     // }),
    //   ],
    // },
    //
    // {
    //   // Every A.B.C change in your app schema should be a migration
    //   toVersion: 1, // Corresponds to the initial schema version in localAppSchema
    //   steps: [
    //     // Since localAppSchema defines version 1, if this is the first run,
    //     // WatermelonDB will use the schema directly.
    //     // If you were migrating from version 0 (no schema) to version 1,
    //     // you would define the creation of all tables here.
    //     // For a fresh app, this can be empty if localAppSchema.version is 1.
    //     // However, it's good practice to explicitly define the initial schema creation
    //     // if you start with version: 0 and migrate to version: 1.
    //     // For now, we assume localAppSchema.version: 1 is the initial state.
    //     // If localAppSchema.version is already 1, WatermelonDB handles it.
    //     // If you start with no schema (version 0 implicitly) and localAppSchema.version is 1,
    //     // then you'd need a migration to version 1 that creates all tables.
    //     //
    //     // Example for an initial schema creation (if you were migrating from 0 to 1):
    //     // ...localAppSchema.tables.map(table => createTable(table))
    //     //
    //     // Given the SDS, `schema.ts` defines the initial schema at version 1.
    //     // So, migrations might start from version 2 for subsequent changes.
    //     // This file is primarily a placeholder for future migrations.
    //   ],
    // },
  ],
});

// If localAppSchema.version is 1 and this is the first time the app runs with this schema,
// WatermelonDB will create the database according to localAppSchema.
// Migrations are for evolving the schema *after* its initial creation.
// So, for the very first version, this migration array can be empty.
// Future changes to localAppSchema (incrementing its version) will require corresponding migration steps here.

export default migrations;