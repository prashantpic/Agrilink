import { Database } from '@nozbe/watermelondb';
import SQLiteAdapter from '@nozbe/watermelondb/adapters/sqlite';
// import { Platform } from 'react-native'; // If platform-specific paths are needed

// These will be created in subsequent steps/files.
// For now, we assume their existence and structure.
import schema from './schema'; // REQ-14-001: Schema defines local data structure
import migrations from './migrations'; // For schema versioning

// For REQ-14-007: Local Database Encryption
// Integration with SQLite encryption (e.g., SQLCipher) would happen here.
// This often involves using a specific build of react-native-sqlite-storage
// or a dedicated library. The encryption key should be securely managed,
// possibly via react-native-keychain or platform's secure storage.

// Example: Placeholder for encryption key alias from environment/secure storage
// const LOCAL_DB_ENCRYPTION_KEY_ALIAS = 'com.thesss.platform.db_encryption_key';

const adapter = new SQLiteAdapter({
  schema,
  migrations,
  // dbName: 'MobileAppDB', // Optional: specify database name
  jsi: true, // JSI is a new WatermelonDB feature that improves performance.
             // true: use JSI (recommended), false: fall back to synchronous bridge.
             // Optional: Platform.OS === 'ios' // JSI only for iOS for example
  onSetUpError: error => {
    // Handle fatal database setup errors (e.g., if migrations fail)
    console.error('WatermelonDB setup failed:', error);
    // Potentially, you could trigger a UI update or a specific error handling routine.
  },
  // REQ-14-007: Local Database Encryption
  // The `react-native-sqlite-storage` adapter for WatermelonDB might accept
  // encryption options directly, or you might need a custom adapter version.
  // Example (conceptual, actual options depend on the SQLite plugin used):
  // encryptionKey: 'your-super-secret-key', // THIS IS INSECURE. Key should be fetched from secure storage.
  // or using options for react-native-sqlcipher-storage
  // key: await getEncryptionKeyFromKeychain(LOCAL_DB_ENCRYPTION_KEY_ALIAS),
  // sqlcipherVersion: '4', // e.g.
});

// Then, make a Watermelon Database from it!
const database = new Database({
  adapter,
  modelClasses: [
    // Import and list all your WatermelonDB models here.
    // e.g., User, Post from your schema.js/models/
    // These will be defined in files like src/core/data/local/models/Farmer.ts etc.
    // For now, this array will be empty until models are generated.
  ],
  // actionsEnabled: true, // Optional: Enables WatermelonDB actions for more complex operations
});

export default database;

// Helper function placeholder for fetching encryption key (conceptual)
// async function getEncryptionKeyFromKeychain(alias: string): Promise<string> {
//   // Use react-native-keychain or similar to retrieve the key
//   // If key doesn't exist, generate it and store it.
//   // This is a critical security step.
//   // For example:
//   // const credentials = await Keychain.getGenericPassword({ service: alias });
//   // if (credentials) {
//   //   return credentials.password;
//   // } else {
//   //   const newKey = generateSecureRandomKey(); // Implement this
//   //   await Keychain.setGenericPassword('encryptionKeyUser', newKey, { service: alias });
//   //   return newKey;
//   // }
//   return "dummy-key-for-dev"; // Replace with actual secure key management
// }