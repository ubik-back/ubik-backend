import { Component, OnInit } from '@angular/core';
import { UserProfileService } from './services/user-profile.services';
import { ModelProfileComponent } from './components/model-profile/model-profile.component';
import { UserProfile } from './models/user-profile.model';

@Component({
  selector: 'app-user-profile',
  standalone: true,
  imports: [ModelProfileComponent],
  templateUrl: './user-profile.html',
})
export class UserProfilePage implements OnInit {
  profile!: UserProfile;

  constructor(private userProfileService: UserProfileService) {}

  ngOnInit(): void {
    this.userProfileService.getProfile().subscribe({
      next: (profile) => (this.profile = profile),
      error: (err) => console.error('Error cargando perfil', err),
    });
  }
}