import { Component, OnInit } from '@angular/core';
import { Card } from "../../components/card/card";
import { HabitacionInformacion } from '../../components/card/card';
import { Button01 } from '../../components/button-01/button-01';
import { Button02 } from '../../components/button-02/button-02';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-rooms-motel',
  imports: [Card, RouterLink],
  templateUrl: './rooms-motel.html',
  styleUrl: './rooms-motel.css',
})
export class RoomsMotel implements OnInit {

  habitaciones: HabitacionInformacion[] = [];

  ngOnInit(): void {
    this.cargarHabitaciones();
  }

  /** -----------------------------------------------------------
   *  🔥 Datos simulados (mock) — reemplaza luego por tu backend
   *  -----------------------------------------------------------
   */
  cargarHabitaciones(): void {
    this.habitaciones = [
      {
        id: 1,
        motelId: 10,
        numberHab: "101",
        type: "Suite Premium",
        price: 150000,
        descripcion: "Jacuzzi, luces led y parqueadero privado.",
        imagen: "https://images.unsplash.com/photo-1600585154340-be6161a56a0c"
      },
      {
        id: 2,
        motelId: 10,
        numberHab: "102",
        type: "Habitación Deluxe",
        price: 120000,
        descripcion: "Ambiente moderno, minibar y aire acondicionado.",
        imagen: "https://images.unsplash.com/photo-1600585154154-1c1af7c8c3dc"
      },
      {
        id: 3,
        motelId: 10,
        numberHab: "103",
        type: "Suite Ejecutiva",
        price: 180000,
        descripcion: "Ideal para ocasiones especiales, incluye botella.",
        imagen: "https://images.unsplash.com/photo-1551776235-dde6d4829808"
      }
    ];
  }

}