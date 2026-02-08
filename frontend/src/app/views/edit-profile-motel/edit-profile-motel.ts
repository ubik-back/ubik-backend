import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Inputcomponent } from '../../components/input/input';

import { MotelMockService, InfoPerfile } from '../../core/services/motel/motel-mock';
import { Button01 } from '../../components/button-01/button-01';

@Component({
  selector: 'app-edit-profile-motel',
  standalone: true,
  imports: [
    CommonModule,
    Inputcomponent,
    Button01
  ],
  templateUrl: './edit-profile-motel.html',
})
export class EditProfileMotelComponent implements OnInit {
  private motelService = inject(MotelMockService);

  profile!: InfoPerfile;

  ngOnInit(): void {
    this.motelService.getProfile().subscribe((data) => {
      this.profile = { ...data };
    });
  }

  saveProfile(): void {
    console.log('Perfil a guardar:', this.profile);
  }
}