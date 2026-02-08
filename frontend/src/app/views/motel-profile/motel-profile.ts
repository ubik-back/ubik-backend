import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Button01 } from '../../components/button-01/button-01';
import { CardOffers, CardOff } from '../../components/card-offers/card-offers';
import { CardRoom } from '../../components/card-room/card-room';
import { MotelMockService } from '../../core/services/motel/motel-mock';
import { InfoPerfile, CardHabitacion } from '../../core/services/motel/motel-mock';



@Component({
  selector: 'app-perfile-motel',
  standalone: true,
  imports: [Button01, CommonModule, CardOffers, CardRoom],
  templateUrl: './motel-profile.html',
  styleUrls: ['./motel-profile.css'],
})


export class MotelProfile implements OnInit {
  private motelService = inject(MotelMockService);

  profile!: InfoPerfile;
  ofertas: CardOff[] = [];
  CardHab: CardHabitacion[] = [];

  loading = true;

  ngOnInit(): void {
    this.motelService.getProfile().subscribe((data) => {
      this.profile = data;
    });

    this.motelService.getOffers().subscribe((data) => {
      this.ofertas = data;
    });

    this.motelService.getRooms().subscribe((data) => {
      this.CardHab = data;
      this.loading = false;
    });
  }
}