import { Component } from '@angular/core';
import { Logo01 } from "../components/logo-01/logo-01";
import { Card3 } from '../components/card-3/card-3';

@Component({
  selector: 'app-todos-los-componentes',
  imports: [Logo01, Card3],
  templateUrl: './todos-los-componentes.html',
  styleUrl: './todos-los-componentes.css',
})
export class TodosLosComponentes {

}
