import { Component, Input } from '@angular/core';
import { Button01 } from '../button-01/button-01';
import { Button02 } from '../button-02/button-02';

export interface HabitacionInformacion {
  id: number;
  motelId: number;
  numberHab: string;
  type: string;
  price: number;
  descripcion: string;
  imagen: string;
}

@Component({
  selector: 'app-card',
  imports: [Button01, Button02],
  templateUrl: './card.html',
})
export class Card {

  @Input() card!: HabitacionInformacion;
  @Input() textButton1: string = 'Reservar';  
  @Input() textButton2: string = 'Detalles'; 
  @Input() showDescription: boolean = true;  

}
