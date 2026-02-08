import { Component, OnInit, inject, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { Card, HabitacionInformacion } from '../../components/card/card';
import { RoomService } from '../../core/services/room/room';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, Card],
  templateUrl: './home.html',
})

export class Home implements OnInit {

  private platformId = inject(PLATFORM_ID);

  mejoresOfertas: HabitacionInformacion[] = [];
  motelesCercanos: HabitacionInformacion[] = [];
  destinosPopulares: HabitacionInformacion[] = [];

  constructor(private roomService: RoomService) {}

  ngOnInit(): void {
    if (isPlatformBrowser(this.platformId)) {
      this.cargarRooms();
    }
  }

  cargarRooms(): void {
    this.roomService.getRooms().subscribe({
      next: (rooms) => {
        console.log('Rooms desde backend:', rooms);

        const cards: HabitacionInformacion[] = rooms
          .filter(room => room.isAvailable)
          .map(room => ({
            id: room.id,
            motelId: room.motelId,
            numberHab: room.number,
            type: room.roomType,
            descripcion: room.description,
            imagen: room.imageUrls?.[0] || 'assets/images/ubikLogo.jpg',
            price: room.price,
            isAvailable: room.isAvailable
          }));

        this.mejoresOfertas = [...cards]
          .sort((a, b) => a.price - b.price)
          .slice(0, 5);

        this.motelesCercanos = cards.filter(c => c.id <= 4);

        this.destinosPopulares = cards.slice(0, 5);
      },
      error: (error) => {
        console.error('Error cargando rooms', error);
      }
    });
  }
}