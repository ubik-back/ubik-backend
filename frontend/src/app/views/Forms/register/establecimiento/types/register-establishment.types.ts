export interface RegisterEstablishmentForm {
  name: string;
  address: string;
  phoneNumber: string;
  description: string;
  city: string;

  latitude: number | null;
  longitude: number | null;

  images: File[];
}

export interface RegisterEstablishmentPayload {
  name: string;
  address: string;
  phoneNumber: string;
  description: string;
  city: string;

  propertyId: number;

  imageUrls: string[];

  latitude: number;
  longitude: number;
}


export interface LocationInfo {
  establishmentName: string;
  establishmentEmail: string;
  establishmentPhone: string;
  establishmentDescription: string;

  rue: string;
  rnt: string;

  country: string;
  department: string;
  municipality: string;
  address: string;

  password: string;
  confirmPassword: string;
}