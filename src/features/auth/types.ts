export interface UserSession {
  username: string;
  name: string;
  mustChangePassword: boolean;
}

export interface LoginInput {
  username: string;
  password: string;
}

export interface ChangePasswordInput {
  currentPassword: string;
  newPassword: string;
}
