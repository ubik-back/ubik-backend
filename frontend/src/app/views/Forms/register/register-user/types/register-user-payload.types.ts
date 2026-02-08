export interface RegisterUserPayload {
  username: string;
  password: string;
  email: string;
  phoneNumber: string;
  anonymous: boolean;
  roleId: number;
  longitude: number;
  latitude: number;
  birthDate: string;
}