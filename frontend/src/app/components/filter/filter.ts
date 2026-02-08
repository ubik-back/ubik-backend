import { Dialog } from '@angular/cdk/dialog';
import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';


/*========= SIMULACION DE MODELOS ============*/

export interface Feature {
  id: number;
  name: string;
  icon: string;
};

export interface Category {
  id: number;
  name: string;
};

export interface Location {
  id: number;
  city: string;
};

export interface Room {
  id: number;
  name: string;
  category: Category;
  price: number;
  features: Feature[];
}

export interface Motel {
  id: number;
  name: string;
  adress: string;
  location: Location[];
  rooms: Room[];
}

/*========= SIMULACION DE TABLAS DE BASES DE DATOS FEATURES Y CATEGOTIAS ===============*/

const FEATURES: Feature[] = [
  { id: 1, name: 'Jacuzzi', icon: 'pi pi-star' },
  { id: 2, name: 'Parqueadero', icon: 'pi pi-car' },
  { id: 3, name: 'TV', icon: 'pi pi-desktop' },
  { id: 4, name: 'Aire acondicionado', icon: 'pi pi-snowflake' }
];

const CATEGORIES: Category[] = [
  { id: 1, name: 'Premium' },
  { id: 2, name: 'Económico' }
];

const LOCATIONS: Location[] = [
  {id:1, city: 'Bogota'},
  {id: 2,  city: 'Medellin'},
  {id: 3,  city: 'Armenia'},

]

@Component({
  selector: 'app-filter',
  imports: [CommonModule],
  templateUrl: './filter.html',
})
export class Filter{


  FEATURES = FEATURES;
  CATEGORIES = CATEGORIES;
  LOCATIONS = LOCATIONS;

/*========= SIMULACION DE TABLAS DE BASES DE DATOS Habitaciones del motel ===============*/

  motels: Motel[] = [

    { id: 1,
      name: 'Oasis',
      location: [LOCATIONS[1]],
      adress: 'Los naranjos',
      rooms: [
        {
          id: 1,
          name: 'Suite Jacuzzi',
          category: CATEGORIES[0],
          price: 35000,
          features: [FEATURES[0], FEATURES[1], FEATURES[2]]
        },
        {
          id: 2,
          name: 'Habitación Estándar',
          category: CATEGORIES[1],
          price: 50000,
          features: [FEATURES[1], FEATURES[2]]
        }
      ]
    },
    {
      id: 2,
      name: 'Luna Azul',
      location: [LOCATIONS[2]],
      adress: 'Barrio del bajo mundo',
      rooms: [
        {
          id: 3,
          name: 'Habitación Premium',
          category: CATEGORIES[0],
          price: 45000,
          features: [FEATURES[0], FEATURES[2]]
        },
        {
          id: 4,
          name: 'Habitación Básica',
          category: CATEGORIES[1],
          price: 45000,
          features: [FEATURES[1]]
        }
      ]
    }
  ];

  /** Logica de los filtros */

  filters = {
    categoryId: null as number | null,
    locationId: null as number | null,
    priceMin: 0,
    priceMax: 50000,
    featureIds: [] as number[]
  };

  onFeatureChange(event: Event) {
  const input = event.target as HTMLInputElement;
  const value = Number(input.value);

  if (input.checked) {
    this.filters.featureIds.push(value);
  } else {
    this.filters.featureIds =
      this.filters.featureIds.filter(id => id !== value);
  }
  }
  onCategoryChange(event: Event) {
    const input = event.target as HTMLInputElement;
    this.filters.categoryId = input.checked
      ? Number(input.value)
      : null;
  }
  onLocationChange(event: Event) {
  const input = event.target as HTMLInputElement;
  this.filters.locationId = input.checked
    ? Number(input.value)
    : null;
  }
  onPriceChange(event: Event) {
  const input = event.target as HTMLInputElement;
  this.filters.priceMax = Number(input.value);
  }

  get filteredRooms() {
  return this.motels.flatMap(motel =>

    motel.rooms
      .filter(room =>

        (this.filters.categoryId
          ? room.category.id === this.filters.categoryId
          : true) &&

        (this.filters.locationId
          ? motel.location.some(l => l.id === this.filters.locationId)
          : true) &&

        room.price === this.filters.priceMax &&

        this.filters.featureIds.every(id =>
          room.features.some(f => f.id === id)
        )
      )
      .map(room => ({
        motelName: motel.name,
        location: motel.location,
        adress: motel.adress,
        room
      }))
  );
  }  
}