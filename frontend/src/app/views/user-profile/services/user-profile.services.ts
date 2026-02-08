import { Injectable, inject, PLATFORM_ID } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, EMPTY } from 'rxjs';
import { isPlatformBrowser } from '@angular/common';

import { UserProfile } from '../models/user-profile.model';
import { environment } from '../../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class UserProfileService {

  private http = inject(HttpClient);
  private platformId = inject(PLATFORM_ID);

  private baseUrl = `${environment.apiUrl}/user`;

  getProfile(): Observable<UserProfile> {
    
    if (!isPlatformBrowser(this.platformId)) {
      return EMPTY; 
    }

    return this.http.get<UserProfile>(this.baseUrl);
  }
}