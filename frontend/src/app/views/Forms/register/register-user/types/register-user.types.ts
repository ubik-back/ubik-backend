export interface RegisterFormData {
  username: string;
  password: string;
  comfirmPassword: string;
  email: string;
  phoneNumber: string;
  anonymous: boolean;
  roleId: number;
  longitude: number;
  latitude: number;
  birthDate: string;
}

export interface ValidationError {
  field: string;
  message: string;
}