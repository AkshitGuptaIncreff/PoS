export type UserRole = 'SUPERVISOR' | 'OPERATOR';

export interface SessionData {
    sessionId: string;
    email: string;
    role: UserRole;
    lastCheckTime: number; // timestamp
}

export interface AuthResponse {
    email: string;
    role: UserRole;
    sessionId: string;
}

export interface ValidateResponse {
    email: string;
    role: UserRole;
}