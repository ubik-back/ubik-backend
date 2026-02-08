import { Component, Input } from '@angular/core';
import { UserProfile } from '../../models/user-profile.model';
import { Button01 } from '../../../../components/button-01/button-01';

@Component({
  selector: 'app-model-profile',
  imports: [Button01],
  templateUrl: './model-profile.component.html',
})
export class ModelProfileComponent {
  @Input() profile!: UserProfile;
}