import { Component, Input } from '@angular/core';
import { Button01 } from "../button-01/button-01";

@Component({
  selector: 'app-card-3',
  imports: [Button01],
  templateUrl: './card-3.html',
})
export class Card3 {

  @Input() image!: string;
  @Input() title!: string;
  @Input() location!: string;
  @Input() adress!: string;
 
  @Input() price!: number | string;
  @Input() hours!: number;

  @Input() services: {
    id: number;
    name: string;
    icon: string;
  }[] = [];

}
