import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { HttpHeaders } from '@angular/common/http';
import { environment } from '../../../../../../environments/environment';

import { RegisterEstablishmentPayload } from '../types/register-establishment.types';

@Injectable({
  providedIn: 'root',
})
export class EstablishmentService {

  private readonly REGISTER_URL = `${environment.apiUrl}/auth/register`;

  constructor(private http: HttpClient) {}

  /**
   * Crear un establecimiento
   */
  createEstablishment(
    payload: RegisterEstablishmentPayload
  ): Observable<void> {

    const headers = new HttpHeaders({
      Authorization: `Bearer ${localStorage.getItem('token')}`
    });

    return this.http.post<void>(this.REGISTER_URL, payload, { headers });
  }
}