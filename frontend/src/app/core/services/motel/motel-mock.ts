import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { map } from 'rxjs/operators';

/* 🔹 Interfaces basadas en la BD real */
export interface Service {
  id: number;
  name: string;
}

export interface RoomService {
  room_id: number;
  service_id: number;
}

export interface Photo {
  id: number;
  url: string;
  motel_id: number;
  description?: string;
  date_create: Date;
}

export interface Room {
  id: number;
  motel_id: number;
  num_or_name: string;
  room_type: string;
  price: number;
  description: string;
  services?: Service[];
  photos?: Photo[];
}

export interface Motel {
  id: number;
  name: string;
  address: string;
  phone: string;
  description: string;
  city: string;
  property_owner_id: number;
  date_create: Date;
  nit: string;
  rooms?: Room[];
  photos?: Photo[];
}

/* 🔹 Interfaces para la UI (compatibilidad con tu código existente) */
export interface InfoPerfile {
  imageBack: string;
  imagePerfile: string;
  nombre: string;
  ubicacion: string;
  description: string;
  address: string;
  phone?: string;
}

export interface CardHabitacion {
  id: number;
  nombre: string;
  number: number;
  tipo: string;
  servicios: string[];
  descripcion: string;
  imagen: string;
  price: number;
}

export interface CardOff {
  id: number;
  image: string;
  nombre: string;
  descripcion: string;
}

@Injectable({
  providedIn: 'root',
})
export class MotelMockService {
  // ========= DATOS MOCK (simulación de BD) ===============

  private readonly SERVICES: Service[] = [
    { id: 1, name: 'Jacuzzi' },
    { id: 2, name: 'Parqueadero' },
    { id: 3, name: 'TV' },
    { id: 4, name: 'Aire acondicionado' },
    { id: 5, name: 'WiFi' },
    { id: 6, name: 'Minibar' },
    { id: 7, name: 'Música ambiente' },
    { id: 8, name: 'Ducha lluvia' },
  ];

  private readonly ROOM_SERVICES: RoomService[] = [
    { room_id: 1, service_id: 1 },
    { room_id: 1, service_id: 2 },
    { room_id: 1, service_id: 3 },
    { room_id: 1, service_id: 4 },
    { room_id: 1, service_id: 5 },
    { room_id: 2, service_id: 1 },
    { room_id: 2, service_id: 2 },
    { room_id: 2, service_id: 3 },
    { room_id: 2, service_id: 6 },
    { room_id: 3, service_id: 2 },
    { room_id: 3, service_id: 3 },
    { room_id: 3, service_id: 5 },
    { room_id: 4, service_id: 1 },
    { room_id: 4, service_id: 2 },
    { room_id: 4, service_id: 3 },
    { room_id: 4, service_id: 4 },
    { room_id: 4, service_id: 5 },
    { room_id: 4, service_id: 6 },
    { room_id: 4, service_id: 7 },
    { room_id: 4, service_id: 8 },
  ];

  private readonly PHOTOS: Photo[] = [
    // Fotos del motel
    {
      id: 1,
      url: 'https://res.cloudinary.com/du4tcug9q/image/upload/v1764684206/Backimage_dlcpin.png',
      motel_id: 1,
      description: 'Imagen principal',
      date_create: new Date('2024-01-15'),
    },
    {
      id: 2,
      url: 'https://res.cloudinary.com/du4tcug9q/image/upload/v1764684201/profileImage_nax8f9.png',
      motel_id: 1,
      description: 'Logo',
      date_create: new Date('2024-01-15'),
    },
    // Fotos habitación 1
    {
      id: 10,
      url: 'https://res.cloudinary.com/du4tcug9q/image/upload/v1764941479/Habitacion_dyqnb3.png',
      motel_id: 1,
      description: 'Suite Deluxe 101',
      date_create: new Date('2024-01-15'),
    },
    {
      id: 11,
      url: 'https://res.cloudinary.com/du4tcug9q/image/upload/v1764772908/103-3_ccb4iu.jpg',
      motel_id: 1,
      description: 'Suite Deluxe 101 - Jacuzzi',
      date_create: new Date('2024-01-15'),
    },
    // Fotos habitación 2
    {
      id: 12,
      url: 'https://res.cloudinary.com/du4tcug9q/image/upload/v1764941479/Habitacion_dyqnb3.png',
      motel_id: 1,
      description: 'Suite Deluxe 102',
      date_create: new Date('2024-01-15'),
    },
    // Fotos habitación 3
    {
      id: 13,
      url: 'https://images.unsplash.com/photo-1595526114035-0d45ed16cfbf?w=800',
      motel_id: 1,
      description: 'Habitación Económica 103',
      date_create: new Date('2024-01-15'),
    },
    // Fotos habitación 4
    {
      id: 14,
      url: 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=800',
      motel_id: 1,
      description: 'Suite Premium 201',
      date_create: new Date('2024-01-15'),
    },
  ];

  private readonly ROOMS: Room[] = [
    {
      id: 1,
      motel_id: 1,
      num_or_name: '101',
      room_type: 'Suite Deluxe',
      price: 120000,
      description: 'Una habitación de lujo con jacuzzi privado y todas las comodidades.',
    },
    {
      id: 2,
      motel_id: 1,
      num_or_name: '102',
      room_type: 'Suite Deluxe',
      price: 120000,
      description:
        'Acogedor y discreto, ideal para escapadas rápidas o momentos de privacidad.',
    },
    {
      id: 3,
      motel_id: 1,
      num_or_name: '103',
      room_type: 'Económica',
      price: 60000,
      description: 'Habitación cómoda y acogedora a precio accesible.',
    },
    {
      id: 4,
      motel_id: 1,
      num_or_name: '201',
      room_type: 'Suite Premium',
      price: 180000,
      description:
        'La experiencia definitiva en lujo y confort. Incluye todos nuestros servicios premium.',
    },
  ];

  private readonly MOTEL: Motel = {
    id: 1,
    name: 'MOTEL PARADISE',
    address: 'Km 1 via tebaida',
    phone: '+57 316 123 4567',
    description:
      'Acogedor y discreto, ideal para escapadas rápidas o momentos de privacidad. ' +
      'Ofrece habitaciones confortables, servicio ágil y tarifas accesibles, todo en un ambiente seguro y tranquilo.',
    city: 'Armenia',
    property_owner_id: 1,
    date_create: new Date('2024-01-15'),
    nit: '900123456-7',
  };

  private readonly OFFERS: CardOff[] = [
    {
      id: 1,
      image:
        'https://res.cloudinary.com/du4tcug9q/image/upload/v1764772908/103-3_ccb4iu.jpg',
      nombre: 'Aniversario especial',
      descripcion:
        'Celebra tu aniversario con nosotros y disfruta de una noche inolvidable llena de sorpresas y romanticismo.',
    },
    {
      id: 2,
      image:
        'https://res.cloudinary.com/du4tcug9q/image/upload/v1764772908/103-3_ccb4iu.jpg',
      nombre: 'Escapada romántica',
      descripcion:
        'Disfruta de una escapada romántica con tu pareja en nuestras cómodas habitaciones y servicios exclusivos.',
    },
    {
      id: 3,
      image:
        'https://res.cloudinary.com/du4tcug9q/image/upload/v1764772908/103-3_ccb4iu.jpg',
      nombre: 'Fin de semana largo',
      descripcion:
        'Aprovecha el fin de semana largo para relajarte y desconectar en nuestro motel con tarifas especiales.',
    },
  ];

  // ========= MÉTODOS PRIVADOS (simulan queries de BD) ===============

  private getRoomServices(roomId: number): Service[] {
    const serviceIds = this.ROOM_SERVICES.filter((rs) => rs.room_id === roomId).map(
      (rs) => rs.service_id
    );
    return this.SERVICES.filter((s) => serviceIds.includes(s.id));
  }

  private getRoomPhotos(roomId: number): Photo[] {
    const room = this.ROOMS.find((r) => r.id === roomId);
    if (!room) return [];

    return this.PHOTOS.filter(
      (p) =>
        p.motel_id === room.motel_id &&
        p.description?.includes(room.num_or_name)
    );
  }

  private getMotelMainPhotos(): { back: string; profile: string } {
    const backPhoto = this.PHOTOS.find(
      (p) => p.motel_id === this.MOTEL.id && p.description === 'Imagen principal'
    );
    const profilePhoto = this.PHOTOS.find(
      (p) => p.motel_id === this.MOTEL.id && p.description === 'Logo'
    );

    return {
      back:
        backPhoto?.url ||
        'https://res.cloudinary.com/du4tcug9q/image/upload/v1764684206/Backimage_dlcpin.png',
      profile:
        profilePhoto?.url ||
        'https://res.cloudinary.com/du4tcug9q/image/upload/v1764684201/profileImage_nax8f9.png',
    };
  }

  // ========= MÉTODOS PÚBLICOS (API del servicio) ===============

  /* 🔹 PERFIL DEL MOTEL */
  getProfile(): Observable<InfoPerfile> {
    const photos = this.getMotelMainPhotos();
    return of({
      imageBack: photos.back,
      imagePerfile: photos.profile,
      nombre: this.MOTEL.name,
      ubicacion: `${this.MOTEL.city}, Quindío`,
      description: this.MOTEL.description,
      address: this.MOTEL.address,
      phone: this.MOTEL.phone,
    });
  }

  /* 🔹 OFERTAS */
  getOffers(): Observable<CardOff[]> {
    return of(this.OFFERS);
  }

  /* 🔹 HABITACIONES (formato para cards) */
  getRooms(): Observable<CardHabitacion[]> {
    return of(this.ROOMS).pipe(
      map((rooms) =>
        rooms.map((room) => {
          const services = this.getRoomServices(room.id);
          const photos = this.getRoomPhotos(room.id);
          const mainPhoto = photos[0]?.url || 'https://via.placeholder.com/400x300';

          return {
            id: room.id,
            nombre: room.room_type,
            number: parseInt(room.num_or_name) || 0,
            tipo: room.room_type,
            servicios: services.map((s) => s.name),
            descripcion: room.description,
            imagen: mainPhoto,
            price: room.price,
          };
        })
      )
    );
  }

  /* 🔹 OBTENER HABITACIÓN POR ID (para vista de detalle) */
  getRoomById(roomId: number): Observable<Room | undefined> {
    const room = this.ROOMS.find((r) => r.id === roomId);
    if (!room) return of(undefined);

    const roomWithDetails: Room = {
      ...room,
      services: this.getRoomServices(roomId),
      photos: this.getRoomPhotos(roomId),
    };

    return of(roomWithDetails);
  }

  /* 🔹 OBTENER MOTEL COMPLETO */
  getMotel(): Observable<Motel> {
    const motelRooms = this.ROOMS.map((room) => ({
      ...room,
      services: this.getRoomServices(room.id),
      photos: this.getRoomPhotos(room.id),
    }));

    const motelPhotos = this.PHOTOS.filter(
      (p) =>
        p.motel_id === this.MOTEL.id &&
        (p.description === 'Imagen principal' || p.description === 'Logo')
    );

    return of({
      ...this.MOTEL,
      rooms: motelRooms,
      photos: motelPhotos,
    });
  }

  /* 🔹 FILTRAR HABITACIONES POR TIPO */
  getRoomsByType(roomType: string): Observable<Room[]> {
    const filteredRooms = this.ROOMS.filter((r) => r.room_type === roomType).map(
      (room) => ({
        ...room,
        services: this.getRoomServices(room.id),
        photos: this.getRoomPhotos(room.id),
      })
    );
    return of(filteredRooms);
  }

  /* 🔹 FILTRAR HABITACIONES POR RANGO DE PRECIO */
  getRoomsByPriceRange(min: number, max: number): Observable<Room[]> {
    const filteredRooms = this.ROOMS.filter(
      (r) => r.price >= min && r.price <= max
    ).map((room) => ({
      ...room,
      services: this.getRoomServices(room.id),
      photos: this.getRoomPhotos(room.id),
    }));
    return of(filteredRooms);
  }

  /* 🔹 OBTENER TODOS LOS SERVICIOS DISPONIBLES */
  getAllServices(): Observable<Service[]> {
    return of(this.SERVICES);
  }
}