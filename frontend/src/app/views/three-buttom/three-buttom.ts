import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Button01 } from '../../components/button-01/button-01';

@Component({
  selector: 'app-three-buttom',
  imports: [Button01, CommonModule],
  templateUrl: './three-buttom.html',
  styleUrl: './three-buttom.css',
})
export class ThreeButtom {
  habitaciones: string = 'Habitaciones';
  Ofertas: string = 'Ofertas';
  Perfil: string = 'Perfil';
}
