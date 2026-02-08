import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class RoomService {

  private baseUrl = `${environment.apiUrl}/rooms`;

  constructor(private http: HttpClient) {}

  // Obtener todas las habitaciones
  getRooms(): Observable<any[]> {
    return this.http.get<any[]>(this.baseUrl);
  }

  // Habitaciones por motel
  getRoomsByMotel(motelId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/motel/${motelId}`);
  }

  // Habitaciones disponibles por motel
  getAvailableRooms(motelId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/motel/${motelId}/available`);
  }
}