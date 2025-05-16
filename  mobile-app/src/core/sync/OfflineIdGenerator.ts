import { v4 as uuidv4 } from 'uuid';

/**
 * Generates a universally unique identifier (UUID v4).
 * REQ-14-003: Used for new records created while the application is offline,
 * ensuring global uniqueness before server sync.
 * @returns A string representing the generated UUID.
 */
export function generateUUID(): string {
  return uuidv4();
}

const OfflineIdGenerator = {
  generateUUID,
};

export default OfflineIdGenerator;