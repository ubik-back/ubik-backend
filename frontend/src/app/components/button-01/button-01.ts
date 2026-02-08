import { Component, Input, Output, EventEmitter} from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

@Component({
  selector: 'app-button-01',
  imports: [CommonModule],
  templateUrl: './button-01.html', 
})
export class Button01 {
  @Input() text!: string;
  @Input() routerLink?: string | (string | number)[];
  @Input() iconLeft?: string;
  @Input() iconRight?: string;
  @Input() id!: number; 
  @Input() action!: 'reservar' | 'detalles';
  @Input() disabled = false;
  @Input() type!: 'submit' | 'button';

  // si es true el botón ocupará el 100%
  @Input() fullWidth: boolean = false;


  constructor(private router: Router) {}
    

  navigate() {
  if (!this.routerLink) return;

    if (Array.isArray(this.routerLink)) {
      this.router.navigate(this.routerLink);
    } else {
      this.router.navigate([this.routerLink]);
    }
  }
}