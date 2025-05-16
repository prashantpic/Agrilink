export type UserRole = 'Farmer' | 'Admin' | 'Farm Plot Admin' | 'Consultant' | 'Guest'; // Added Guest for unauthenticated

export interface User {
  id: string;
  username: string;
  email?: string; // Optional email
  phone?: string; // Optional phone
  role: UserRole; // As per SDS, single role. If multiple roles possible, change to UserRole[]
  firstName?: string;
  lastName?: string;
  // other user properties relevant to the UI
  // e.g., profileImageUrl, lastLogin, etc.
}

export interface AuthState {
  isAuthenticated: boolean;
  user: User | null;
  token: string | null;
  loading: boolean;
  error: string | null;
}