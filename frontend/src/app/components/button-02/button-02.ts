import { CommonModule } from '@angular/common';
import { Component, Input, Output } from '@angular/core';
import { Router } from '@angular/router';
import { EventEmitter } from '@angular/core';

@Component({
  selector: 'app-button-02',
  imports: [CommonModule],
  templateUrl: './button-02.html',
})
export class Button02 {
  @Output() clicked = new EventEmitter<void>();

  @Input() text!: string;
  @Input() routerLink?: string;
  @Input() iconLeft?: string;
  @Input() iconRight?: string;

  // si es true el botón ocupará el 100%
  @Input() fullWidth: boolean = false;

  constructor(private router: Router) {}

  navigate() {
    if (this.routerLink) {
      this.router.navigate([this.routerLink]);
    }
  }
}