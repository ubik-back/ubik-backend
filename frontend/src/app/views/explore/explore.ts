import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Card3 } from "../../components/card-3/card-3";
import { Button02 } from "../../components/button-02/button-02";
import { Map } from "../../components/map/map";
import { Filter } from '../../components/filter/filter';
import { Dialog } from '@angular/cdk/dialog';

@Component({
  selector: 'app-explore',
  standalone: true,
  imports: [CommonModule, Card3, Button02, Map, Filter],
  templateUrl: './explore.html',
})
export class Explore {

  private dialog = inject(Dialog);
  protected openModal() 
  { this.dialog.open(Filter); } 
  isOpen = false; 
  toggle() { this.isOpen = !this.isOpen; }

  motels = [
    {
      id: 1,
      name: 'Oasis',
      location: [{ id: 2, city: 'Medellin' }],
      adress: 'Los naranjos',
      rooms: [
        { id: 1, name: 'Suite Jacuzzi', category: { id: 1, name: 'Premium' }, price: 35000, features: [{ id: 1, name: 'Jacuzzi', icon: '' }] },
        { id: 2, name: 'Habitación Estándar', category: { id: 2, name: 'Económico' }, price: 50000, features: [{ id: 2, name: 'Parqueadero', icon: '' }] }
      ]
    }
  ];

  filters = {
    categoryId: null,
    locationId: null,
    priceMax: 50000,
    featureIds: [] as number[]
  };

  onFiltersChange(filters: any) {
    this.filters = filters;
  }

  get filteredRooms() {
    return this.motels.flatMap(motel =>
      motel.rooms.filter(room =>
        (!this.filters.categoryId || room.category.id === this.filters.categoryId) &&
        (!this.filters.locationId || motel.location.some(l => l.id === this.filters.locationId)) &&
        room.price <= this.filters.priceMax &&
        this.filters.featureIds.every(id => room.features.some(f => f.id === id))
      ).map(room => ({
        motelName: motel.name,
        location: motel.location,
        adress: motel.adress,
        room
      }))
    );
  }
}
