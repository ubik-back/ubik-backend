import { Component, Input } from '@angular/core';


interface CardHabitacion {
  id: number;
  nombre: string;
  number: number;
  tipo: string;
  servicios: string[];
  descripcion: string;
  imagen: string;
  price: number;
}

@Component({
  selector: 'app-card-room',
  imports: [],
  templateUrl: './card-room.html',
})
export class CardRoom {

  @Input() cardHabitacion!: CardHabitacion; 
  // El ! indica que llegará desde el padre

  constructor() {}
}
