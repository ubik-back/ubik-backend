# Guía de Integración Frontend - Ubik App

Esta guía proporciona toda la información necesaria para integrar el frontend Angular con los microservicios backend de la aplicación Ubik.

## 📋 Índice

1. [Arquitectura General](#arquitectura-general)
2. [Configuración de Entorno](#configuración-de-entorno)
3. [API Gateway](#api-gateway)
4. [Autenticación y Autorización](#autenticación-y-autorización)
5. [API de User Management](#api-de-user-management)
6. [API de Motel Management](#api-de-motel-management)
7. [API de Products](#api-de-products)
8. [Estructuras de Datos (DTOs)](#estructuras-de-datos-dtos)
9. [Manejo de Errores](#manejo-de-errores)
10. [Ejemplos de Integración](#ejemplos-de-integración)

---

## 🏗️ Arquitectura General

El sistema está compuesto por:

- **Frontend**: Angular 20 con TailwindCSS en el puerto 4200 (desarrollo)
- **API Gateway**: Spring Cloud Gateway en puerto **8080** (punto de entrada único)
- **Microservicios**:
  - `userManagement`: Puerto 8081 (autenticación y usuarios)
  - `products`: Puerto 8082 (productos)
  - `motelManagement`: Puerto 8083 (moteles, habitaciones, servicios, reservas)

### Flujo de Comunicación

```
Frontend (Angular) → API Gateway (8080) → Microservicios (8081, 8082, 8083)
```

**URL Base para todas las peticiones**: `http://localhost:8080/api`

---

## ⚙️ Configuración de Entorno

### Variables de Entorno Necesarias

Para el API Gateway y microservicios:

```bash
# JWT Configuration
JWT_SECRET=tu_clave_secreta_aqui
JWT_EXPIRATION=86400000  # 24 horas en milisegundos

# Database - UserManagement (PostgreSQL)
DB_R2DBC_URL=r2dbc:postgresql://localhost:5432/user_management_db
DB_USERNAME=postgres
DB_PASSWORD=tu_password

# Database - MotelManagement (PostgreSQL)
SPRING_R2DBC_URL=r2dbc:postgresql://localhost:5432/motel_management_db
SPRING_R2DBC_USERNAME=postgres
SPRING_R2DBC_PASSWORD=tu_password
```

### Iniciar los Servicios

```bash
# Desde el directorio microservicios/microreactivo
mvn clean install -DskipTests

# Terminal 1 - Gateway
mvn -pl gateway spring-boot:run

# Terminal 2 - UserManagement
mvn -pl userManagement spring-boot:run

# Terminal 3 - MotelManagement
mvn -pl motelManegement spring-boot:run

# Terminal 4 - Products
mvn -pl products spring-boot:run
```

---

## 🌐 API Gateway

### Configuración CORS

El gateway está configurado para permitir peticiones desde cualquier origen:

- **Allowed Origins**: `*`
- **Allowed Methods**: `GET`, `POST`, `PUT`, `DELETE`, `OPTIONS`
- **Allowed Headers**: `*`
- **Exposed Headers**: `X-User-Id`, `X-User-Role`, `X-Error-Message`
- **Max Age**: 3600 segundos

### Rutas Configuradas

| Ruta                  | Microservicio        | Autenticación | Descripción                           |
|-----------------------|----------------------|---------------|---------------------------------------|
| `/api/auth/**`        | userManagement:8081  | No            | Login, registro, reset password       |
| `/api/user/**`        | userManagement:8081  | Sí            | Perfil de usuario                     |
| `/api/products/**`    | products:8082        | Sí            | Gestión de productos                  |
| `/api/motels/**`      | motelManagement:8083 | No            | Gestión de moteles                    |
| `/api/rooms/**`       | motelManagement:8083 | Sí            | Gestión de habitaciones               |
| `/api/services/**`    | motelManagement:8083 | Sí            | Gestión de servicios                  |
| `/api/reservations/**`| motelManagement:8083 | Sí            | Gestión de reservas                   |

---

## 🔐 Autenticación y Autorización

### Sistema JWT

El sistema utiliza JWT (JSON Web Tokens) para autenticación.

#### 1. Registro de Usuario

**Endpoint**: `POST /api/auth/register`

**Request Body**:
```json
{
  "username": "johndoe",
  "password": "SecurePass123!",
  "email": "john@example.com",
  "anonymous": false,
  "roleId": 1
}
```

**Validaciones**:
- `username`: requerido, no vacío
- `password`: requerido, no vacío
- `email`: requerido, formato email válido
- `anonymous`: booleano requerido
- `roleId`: número positivo mayor o igual a 1

**Response**: `201 CREATED`
```json
"Usuario registrado exitosamente"
```

#### 2. Login

**Endpoint**: `POST /api/auth/login`

**Request Body**:
```json
{
  "username": "johndoe",
  "password": "SecurePass123!"
}
```

**Response**: `200 OK`
```json
"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

El token JWT retornado debe ser almacenado y enviado en las peticiones subsiguientes.

#### 3. Usar el Token JWT

Para endpoints que requieren autenticación, incluir el header:

```
Authorization: Bearer <tu_token_jwt>
```

El gateway valida el token y agrega headers adicionales:
- `X-User-Username`: nombre de usuario
- `X-User-Id`: ID del usuario
- `X-User-Role`: rol del usuario

#### 4. Reset de Contraseña

**Solicitar reset**:
```
POST /api/auth/reset-password-request?email=john@example.com
```

**Resetear contraseña**:
```json
POST /api/auth/reset-password
{
  "email": "john@example.com",
  "token": "reset_token_recibido",
  "newPassword": "NewSecurePass123!"
}
```

---

## 👤 API de User Management

### Obtener Perfil del Usuario Autenticado

**Endpoint**: `GET /api/user`

**Headers**:
```
Authorization: Bearer <token>
```

**Response**: `200 OK`
```json
{
  "id": 1,
  "username": "johndoe",
  "email": "john@example.com",
  "phoneNumber": "+57 300 1234567",
  "createdAt": "2024-01-15T10:30:00",
  "anonymous": false,
  "roleId": 1
}
```

### Actualizar Perfil

**Endpoint**: `PUT /api/user`

**Headers**:
```
Authorization: Bearer <token>
```

**Request Body**:
```json
{
  "email": "newemail@example.com",
  "phoneNumber": "+57 300 7654321"
}
```

**Response**: `200 OK`
```json
{
  "id": 1,
  "username": "johndoe",
  "email": "newemail@example.com",
  "phoneNumber": "+57 300 7654321",
  "createdAt": "2024-01-15T10:30:00",
  "anonymous": false,
  "roleId": 1
}
```

---

## 🏨 API de Motel Management

### Endpoints de Moteles

#### Listar Todos los Moteles

**Endpoint**: `GET /api/motels`

**Response**: `200 OK`
```json
[
  {
    "id": 1,
    "name": "Motel Paradise",
    "address": "Calle Principal 123",
    "phoneNumber": "+57 1 234 5678",
    "description": "El mejor motel de la ciudad",
    "city": "Bogotá",
    "propertyId": 100,
    "dateCreated": "2024-01-15T10:30:00",
    "imageUrls": [
      "https://example.com/image1.jpg",
      "https://example.com/image2.jpg"
    ]
  }
]
```

#### Obtener Motel por ID

**Endpoint**: `GET /api/motels/{id}`

**Response**: `200 OK` - Objeto MotelResponse

#### Obtener Moteles por Ciudad

**Endpoint**: `GET /api/motels/city/{city}`

**Ejemplo**: `GET /api/motels/city/Bogotá`

**Response**: `200 OK` - Array de MotelResponse

#### Crear Motel

**Endpoint**: `POST /api/motels`

**Headers**:
```
Authorization: Bearer <token>
```

**Request Body**:
```json
{
  "name": "Motel Paradise",
  "address": "Calle Principal 123",
  "phoneNumber": "+57 1 234 5678",
  "description": "El mejor motel de la ciudad",
  "city": "Bogotá",
  "propertyId": 100,
  "imageUrls": [
    "https://example.com/image1.jpg"
  ]
}
```

**Validaciones**:
- `name`: 3-100 caracteres, requerido
- `address`: máx 255 caracteres, requerido
- `phoneNumber`: máx 20 caracteres
- `description`: máx 500 caracteres
- `city`: máx 100 caracteres, requerido
- `imageUrls`: máximo 10 URLs, cada una máx 500 caracteres

**Response**: `201 CREATED` - Objeto MotelResponse

#### Actualizar Motel

**Endpoint**: `PUT /api/motels/{id}`

**Headers**:
```
Authorization: Bearer <token>
```

**Request Body**: Igual que CreateMotelRequest

**Response**: `200 OK` - Objeto MotelResponse

#### Eliminar Motel

**Endpoint**: `DELETE /api/motels/{id}`

**Headers**:
```
Authorization: Bearer <token>
```

**Response**: `204 NO CONTENT`

---

### Endpoints de Habitaciones

#### Listar Todas las Habitaciones

**Endpoint**: `GET /api/rooms`

**Headers**:
```
Authorization: Bearer <token>
```

**Response**: `200 OK`
```json
[
  {
    "id": 1,
    "motelId": 1,
    "number": "101",
    "roomType": "SUITE",
    "price": 50000.0,
    "description": "Suite de lujo con vista panorámica",
    "isAvailable": true,
    "imageUrls": [
      "https://example.com/room1.jpg"
    ]
  }
]
```

#### Obtener Habitación por ID

**Endpoint**: `GET /api/rooms/{id}`

**Headers**:
```
Authorization: Bearer <token>
```

**Response**: `200 OK` - Objeto RoomResponse

#### Obtener Habitaciones de un Motel

**Endpoint**: `GET /api/rooms/motel/{motelId}`

**Headers**:
```
Authorization: Bearer <token>
```

**Response**: `200 OK` - Array de RoomResponse

#### Obtener Habitaciones Disponibles de un Motel

**Endpoint**: `GET /api/rooms/motel/{motelId}/available`

**Headers**:
```
Authorization: Bearer <token>
```

**Response**: `200 OK` - Array de RoomResponse

#### Crear Habitación

**Endpoint**: `POST /api/rooms`

**Headers**:
```
Authorization: Bearer <token>
```

**Request Body**:
```json
{
  "motelId": 1,
  "number": "101",
  "roomType": "SUITE",
  "price": 50000.0,
  "description": "Suite de lujo con vista panorámica",
  "imageUrls": [
    "https://example.com/room1.jpg"
  ]
}
```

**Validaciones**:
- `motelId`: requerido
- `number`: máx 20 caracteres, requerido
- `roomType`: máx 50 caracteres, requerido
- `price`: número positivo, requerido
- `description`: máx 500 caracteres
- `imageUrls`: máximo 15 URLs

**Response**: `201 CREATED` - Objeto RoomResponse

#### Actualizar Habitación

**Endpoint**: `PUT /api/rooms/{id}`

**Headers**:
```
Authorization: Bearer <token>
```

**Request Body**: Igual que CreateRoomRequest

**Response**: `200 OK` - Objeto RoomResponse

#### Eliminar Habitación

**Endpoint**: `DELETE /api/rooms/{id}`

**Headers**:
```
Authorization: Bearer <token>
```

**Response**: `204 NO CONTENT`

---

### Endpoints de Servicios

#### Listar Todos los Servicios

**Endpoint**: `GET /api/services`

**Headers**:
```
Authorization: Bearer <token>
```

**Response**: `200 OK`
```json
[
  {
    "id": 1,
    "name": "WiFi",
    "description": "Internet de alta velocidad",
    "icon": "wifi",
    "createdAt": "2024-01-15T10:30:00"
  }
]
```

#### Obtener Servicio por ID

**Endpoint**: `GET /api/services/{id}`

**Headers**:
```
Authorization: Bearer <token>
```

**Response**: `200 OK` - Objeto ServiceResponse

#### Obtener Servicio por Nombre

**Endpoint**: `GET /api/services/name/{name}`

**Headers**:
```
Authorization: Bearer <token>
```

**Response**: `200 OK` - Objeto ServiceResponse

#### Obtener Servicios de una Habitación

**Endpoint**: `GET /api/services/room/{roomId}`

**Headers**:
```
Authorization: Bearer <token>
```

**Response**: `200 OK`
```json
[1, 2, 3]
```
*Retorna un array de IDs de servicios*

#### Crear Servicio

**Endpoint**: `POST /api/services`

**Headers**:
```
Authorization: Bearer <token>
```

**Request Body**:
```json
{
  "name": "WiFi",
  "description": "Internet de alta velocidad",
  "icon": "wifi"
}
```

**Validaciones**:
- `name`: máx 50 caracteres, requerido
- `description`: máx 255 caracteres
- `icon`: máx 50 caracteres

**Response**: `201 CREATED` - Objeto ServiceResponse

#### Asociar Servicio a Habitación

**Endpoint**: `POST /api/services/room/{roomId}/service/{serviceId}`

**Headers**:
```
Authorization: Bearer <token>
```

**Response**: `201 CREATED`

#### Eliminar Servicio de Habitación

**Endpoint**: `DELETE /api/services/room/{roomId}/service/{serviceId}`

**Headers**:
```
Authorization: Bearer <token>
```

**Response**: `204 NO CONTENT`

#### Actualizar Servicio

**Endpoint**: `PUT /api/services/{id}`

**Headers**:
```
Authorization: Bearer <token>
```

**Request Body**: Igual que CreateServiceRequest

**Response**: `200 OK` - Objeto ServiceResponse

#### Eliminar Servicio

**Endpoint**: `DELETE /api/services/{id}`

**Headers**:
```
Authorization: Bearer <token>
```

**Response**: `204 NO CONTENT`

---

### Endpoints de Reservas

#### Listar Todas las Reservas

**Endpoint**: `GET /api/reservations`

**Headers**:
```
Authorization: Bearer <token>
```

**Response**: `200 OK`
```json
[
  {
    "id": 1,
    "roomId": 1,
    "userId": 1,
    "checkInDate": "2024-12-20T14:00:00",
    "checkOutDate": "2024-12-21T12:00:00",
    "status": "PENDING",
    "totalPrice": 50000.0,
    "specialRequests": "Habitación en piso alto",
    "createdAt": "2024-12-15T10:30:00",
    "updatedAt": "2024-12-15T10:30:00"
  }
]
```

#### Estados de Reserva

Las reservas pueden tener los siguientes estados:

- `PENDING`: Pendiente de confirmación
- `CONFIRMED`: Confirmada
- `CHECKED_IN`: Cliente ya hizo check-in
- `CHECKED_OUT`: Cliente ya hizo check-out
- `CANCELLED`: Cancelada

#### Obtener Reserva por ID

**Endpoint**: `GET /api/reservations/{id}`

**Headers**:
```
Authorization: Bearer <token>
```

**Response**: `200 OK` - Objeto ReservationResponse

#### Obtener Reservas por Habitación

**Endpoint**: `GET /api/reservations/room/{roomId}`

**Headers**:
```
Authorization: Bearer <token>
```

**Response**: `200 OK` - Array de ReservationResponse

#### Obtener Reservas por Usuario

**Endpoint**: `GET /api/reservations/user/{userId}`

**Headers**:
```
Authorization: Bearer <token>
```

**Response**: `200 OK` - Array de ReservationResponse

#### Obtener Reservas Activas por Habitación

**Endpoint**: `GET /api/reservations/room/{roomId}/active`

**Headers**:
```
Authorization: Bearer <token>
```

**Response**: `200 OK` - Array de ReservationResponse

#### Obtener Reservas por Estado

**Endpoint**: `GET /api/reservations/status/{status}`

**Ejemplo**: `GET /api/reservations/status/CONFIRMED`

**Headers**:
```
Authorization: Bearer <token>
```

**Response**: `200 OK` - Array de ReservationResponse

#### Verificar Disponibilidad de Habitación

**Endpoint**: `GET /api/reservations/room/{roomId}/available`

**Query Parameters**:
- `checkIn`: fecha-hora en formato ISO 8601 (ej: `2024-12-20T14:00:00`)
- `checkOut`: fecha-hora en formato ISO 8601 (ej: `2024-12-21T12:00:00`)

**Ejemplo**: 
```
GET /api/reservations/room/1/available?checkIn=2024-12-20T14:00:00&checkOut=2024-12-21T12:00:00
```

**Headers**:
```
Authorization: Bearer <token>
```

**Response**: `200 OK`
```json
true
```

#### Crear Reserva

**Endpoint**: `POST /api/reservations`

**Headers**:
```
Authorization: Bearer <token>
```

**Request Body**:
```json
{
  "roomId": 1,
  "userId": 1,
  "checkInDate": "2024-12-20T14:00:00",
  "checkOutDate": "2024-12-21T12:00:00",
  "totalPrice": 50000.0,
  "specialRequests": "Habitación en piso alto"
}
```

**Validaciones**:
- `roomId`: requerido
- `userId`: requerido
- `checkInDate`: requerido, debe ser en el futuro
- `checkOutDate`: requerido, debe ser en el futuro
- `totalPrice`: número positivo, requerido
- `specialRequests`: máx 500 caracteres

**Response**: `201 CREATED` - Objeto ReservationResponse

#### Actualizar Reserva

**Endpoint**: `PUT /api/reservations/{id}`

**Headers**:
```
Authorization: Bearer <token>
```

**Request Body**: Igual que CreateReservationRequest

**Response**: `200 OK` - Objeto ReservationResponse

#### Confirmar Reserva

**Endpoint**: `PATCH /api/reservations/{id}/confirm`

**Headers**:
```
Authorization: Bearer <token>
```

**Response**: `200 OK` - Objeto ReservationResponse

#### Cancelar Reserva

**Endpoint**: `PATCH /api/reservations/{id}/cancel`

**Headers**:
```
Authorization: Bearer <token>
```

**Response**: `200 OK` - Objeto ReservationResponse

#### Check-in

**Endpoint**: `PATCH /api/reservations/{id}/checkin`

**Headers**:
```
Authorization: Bearer <token>
```

**Response**: `200 OK` - Objeto ReservationResponse

#### Check-out

**Endpoint**: `PATCH /api/reservations/{id}/checkout`

**Headers**:
```
Authorization: Bearer <token>
```

**Response**: `200 OK` - Objeto ReservationResponse

#### Eliminar Reserva

**Endpoint**: `DELETE /api/reservations/{id}`

**Headers**:
```
Authorization: Bearer <token>
```

**Response**: `204 NO CONTENT`

*Nota: Solo se pueden eliminar reservas canceladas*

---

## 📦 API de Products

El servicio de productos utiliza un enfoque funcional reactivo.

#### Listar Todos los Productos

**Endpoint**: `GET /api/products`

**Headers**:
```
Authorization: Bearer <token>
```

**Response**: `200 OK`
```json
[
  {
    "id": 1,
    "name": "Producto A",
    "price": 19.99,
    "stock": 100
  }
]
```

#### Obtener Producto por ID

**Endpoint**: `GET /api/products/{id}`

**Headers**:
```
Authorization: Bearer <token>
```

**Response**: `200 OK` - Objeto Product

#### Crear Producto

**Endpoint**: `POST /api/products`

**Headers**:
```
Authorization: Bearer <token>
```

**Request Body**:
```json
{
  "name": "Producto A",
  "price": 19.99,
  "stock": 100
}
```

**Response**: `201 CREATED` - Objeto Product con Location header

#### Actualizar Producto

**Endpoint**: `PUT /api/products/{id}`

**Headers**:
```
Authorization: Bearer <token>
```

**Request Body**: Igual que crear producto

**Response**: `200 OK` - Objeto Product actualizado

#### Eliminar Producto

**Endpoint**: `DELETE /api/products/{id}`

**Headers**:
```
Authorization: Bearer <token>
```

**Response**: `204 NO CONTENT`

---

## 📊 Estructuras de Datos (DTOs)

### User DTOs

#### RegisterRequest
```typescript
interface RegisterRequest {
  username: string;      // Requerido
  password: string;      // Requerido
  email: string;         // Requerido, formato email
  anonymous: boolean;    // Requerido
  roleId: number;        // Requerido, >= 1
}
```

#### LoginRequest
```typescript
interface LoginRequest {
  username: string;      // Requerido
  password: string;      // Requerido
}
```

#### UserProfileResponse
```typescript
interface UserProfileResponse {
  id: number;
  username: string;
  email: string;
  phoneNumber: string;
  createdAt: string;     // ISO 8601 date-time
  anonymous: boolean;
  roleId: number;
}
```

#### UpdateUserRequest
```typescript
interface UpdateUserRequest {
  email?: string;
  phoneNumber?: string;
}
```

### Motel DTOs

#### CreateMotelRequest
```typescript
interface CreateMotelRequest {
  name: string;          // Requerido, 3-100 caracteres
  address: string;       // Requerido, max 255 caracteres
  phoneNumber?: string;  // Max 20 caracteres
  description?: string;  // Max 500 caracteres
  city: string;          // Requerido, max 100 caracteres
  propertyId?: number;
  imageUrls?: string[];  // Max 10 URLs, cada una max 500 caracteres
}
```

#### MotelResponse
```typescript
interface MotelResponse {
  id: number;
  name: string;
  address: string;
  phoneNumber: string;
  description: string;
  city: string;
  propertyId: number;
  dateCreated: string;   // ISO 8601 date-time
  imageUrls: string[];
}
```

### Room DTOs

#### CreateRoomRequest
```typescript
interface CreateRoomRequest {
  motelId: number;       // Requerido
  number: string;        // Requerido, max 20 caracteres
  roomType: string;      // Requerido, max 50 caracteres
  price: number;         // Requerido, > 0
  description?: string;  // Max 500 caracteres
  imageUrls?: string[];  // Max 15 URLs
}
```

#### RoomResponse
```typescript
interface RoomResponse {
  id: number;
  motelId: number;
  number: string;
  roomType: string;
  price: number;
  description: string;
  isAvailable: boolean;
  imageUrls: string[];
}
```

### Service DTOs

#### CreateServiceRequest
```typescript
interface CreateServiceRequest {
  name: string;          // Requerido, max 50 caracteres
  description?: string;  // Max 255 caracteres
  icon?: string;         // Max 50 caracteres
}
```

#### ServiceResponse
```typescript
interface ServiceResponse {
  id: number;
  name: string;
  description: string;
  icon: string;
  createdAt: string;     // ISO 8601 date-time
}
```

### Reservation DTOs

#### CreateReservationRequest
```typescript
interface CreateReservationRequest {
  roomId: number;           // Requerido
  userId: number;           // Requerido
  checkInDate: string;      // Requerido, ISO 8601, futuro
  checkOutDate: string;     // Requerido, ISO 8601, futuro
  totalPrice: number;       // Requerido, > 0
  specialRequests?: string; // Max 500 caracteres
}
```

#### ReservationResponse
```typescript
interface ReservationResponse {
  id: number;
  roomId: number;
  userId: number;
  checkInDate: string;      // ISO 8601 date-time
  checkOutDate: string;     // ISO 8601 date-time
  status: 'PENDING' | 'CONFIRMED' | 'CHECKED_IN' | 'CHECKED_OUT' | 'CANCELLED';
  totalPrice: number;
  specialRequests: string;
  createdAt: string;        // ISO 8601 date-time
  updatedAt: string;        // ISO 8601 date-time
}
```

### Product DTOs

#### Product
```typescript
interface Product {
  id: number;
  name: string;
  price: number;
  stock: number;
}
```

---

## ❌ Manejo de Errores

### Códigos de Estado HTTP

| Código | Significado                     | Cuándo ocurre                                |
|--------|---------------------------------|----------------------------------------------|
| 200    | OK                              | Operación exitosa                            |
| 201    | Created                         | Recurso creado exitosamente                  |
| 204    | No Content                      | Operación exitosa sin contenido de respuesta |
| 400    | Bad Request                     | Validación fallida o datos incorrectos       |
| 401    | Unauthorized                    | Token JWT inválido o expirado                |
| 403    | Forbidden                       | Sin permisos para acceder al recurso         |
| 404    | Not Found                       | Recurso no encontrado                        |
| 500    | Internal Server Error           | Error del servidor                           |

### Headers de Error

El gateway agrega el header `X-Error-Message` en respuestas de error cuando está disponible.

### Formato de Error

```json
{
  "timestamp": "2024-12-15T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "El nombre es requerido",
  "path": "/api/motels"
}
```

### Errores Comunes de Validación

- **400 Bad Request**: Campos requeridos faltantes, formato incorrecto
- **401 Unauthorized**: Token JWT no enviado, inválido o expirado
- **404 Not Found**: ID de recurso no existe

---

## 💻 Ejemplos de Integración

### Service Angular para Autenticación

```typescript
import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = 'http://localhost:8080/api';
  private tokenSubject = new BehaviorSubject<string | null>(
    localStorage.getItem('token')
  );

  constructor(private http: HttpClient) {}

  register(userData: RegisterRequest): Observable<string> {
    return this.http.post(`${this.apiUrl}/auth/register`, userData, {
      responseType: 'text'
    });
  }

  login(credentials: LoginRequest): Observable<string> {
    return this.http.post(`${this.apiUrl}/auth/login`, credentials, {
      responseType: 'text'
    }).pipe(
      tap(token => {
        localStorage.setItem('token', token);
        this.tokenSubject.next(token);
      })
    );
  }

  logout(): void {
    localStorage.removeItem('token');
    this.tokenSubject.next(null);
  }

  getToken(): string | null {
    return this.tokenSubject.value;
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }
}

interface RegisterRequest {
  username: string;
  password: string;
  email: string;
  anonymous: boolean;
  roleId: number;
}

interface LoginRequest {
  username: string;
  password: string;
}
```

### HTTP Interceptor para JWT

```typescript
import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor,
  HttpErrorResponse
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AuthService } from './auth.service';
import { Router } from '@angular/router';

@Injectable()
export class JwtInterceptor implements HttpInterceptor {
  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  intercept(
    request: HttpRequest<any>,
    next: HttpHandler
  ): Observable<HttpEvent<any>> {
    const token = this.authService.getToken();
    
    if (token) {
      request = request.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });
    }

    return next.handle(request).pipe(
      catchError((error: HttpErrorResponse) => {
        if (error.status === 401) {
          this.authService.logout();
          this.router.navigate(['/login']);
        }
        return throwError(() => error);
      })
    );
  }
}
```

### Service Angular para Moteles

```typescript
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class MotelService {
  private apiUrl = 'http://localhost:8080/api/motels';

  constructor(private http: HttpClient) {}

  getAllMotels(): Observable<MotelResponse[]> {
    return this.http.get<MotelResponse[]>(this.apiUrl);
  }

  getMotelById(id: number): Observable<MotelResponse> {
    return this.http.get<MotelResponse>(`${this.apiUrl}/${id}`);
  }

  getMotelsByCity(city: string): Observable<MotelResponse[]> {
    return this.http.get<MotelResponse[]>(`${this.apiUrl}/city/${city}`);
  }

  createMotel(motel: CreateMotelRequest): Observable<MotelResponse> {
    return this.http.post<MotelResponse>(this.apiUrl, motel);
  }

  updateMotel(id: number, motel: CreateMotelRequest): Observable<MotelResponse> {
    return this.http.put<MotelResponse>(`${this.apiUrl}/${id}`, motel);
  }

  deleteMotel(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
```

### Service Angular para Reservas

```typescript
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ReservationService {
  private apiUrl = 'http://localhost:8080/api/reservations';

  constructor(private http: HttpClient) {}

  getAllReservations(): Observable<ReservationResponse[]> {
    return this.http.get<ReservationResponse[]>(this.apiUrl);
  }

  getReservationById(id: number): Observable<ReservationResponse> {
    return this.http.get<ReservationResponse>(`${this.apiUrl}/${id}`);
  }

  getReservationsByUser(userId: number): Observable<ReservationResponse[]> {
    return this.http.get<ReservationResponse[]>(`${this.apiUrl}/user/${userId}`);
  }

  getReservationsByRoom(roomId: number): Observable<ReservationResponse[]> {
    return this.http.get<ReservationResponse[]>(`${this.apiUrl}/room/${roomId}`);
  }

  checkRoomAvailability(
    roomId: number,
    checkIn: Date,
    checkOut: Date
  ): Observable<boolean> {
    const params = new HttpParams()
      .set('checkIn', checkIn.toISOString())
      .set('checkOut', checkOut.toISOString());
    
    return this.http.get<boolean>(
      `${this.apiUrl}/room/${roomId}/available`,
      { params }
    );
  }

  createReservation(reservation: CreateReservationRequest): Observable<ReservationResponse> {
    return this.http.post<ReservationResponse>(this.apiUrl, reservation);
  }

  confirmReservation(id: number): Observable<ReservationResponse> {
    return this.http.patch<ReservationResponse>(`${this.apiUrl}/${id}/confirm`, {});
  }

  cancelReservation(id: number): Observable<ReservationResponse> {
    return this.http.patch<ReservationResponse>(`${this.apiUrl}/${id}/cancel`, {});
  }

  checkIn(id: number): Observable<ReservationResponse> {
    return this.http.patch<ReservationResponse>(`${this.apiUrl}/${id}/checkin`, {});
  }

  checkOut(id: number): Observable<ReservationResponse> {
    return this.http.patch<ReservationResponse>(`${this.apiUrl}/${id}/checkout`, {});
  }
}
```

### Ejemplo de Componente de Login

```typescript
import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-login',
  template: `
    <div class="login-container">
      <h2>Iniciar Sesión</h2>
      <form [formGroup]="loginForm" (ngSubmit)="onSubmit()">
        <div class="form-group">
          <label for="username">Usuario</label>
          <input
            id="username"
            type="text"
            formControlName="username"
            class="form-control"
          />
        </div>
        
        <div class="form-group">
          <label for="password">Contraseña</label>
          <input
            id="password"
            type="password"
            formControlName="password"
            class="form-control"
          />
        </div>
        
        <button type="submit" [disabled]="!loginForm.valid">
          Ingresar
        </button>
        
        <div *ngIf="errorMessage" class="error">
          {{ errorMessage }}
        </div>
      </form>
    </div>
  `
})
export class LoginComponent {
  loginForm: FormGroup;
  errorMessage = '';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.loginForm = this.fb.group({
      username: ['', Validators.required],
      password: ['', Validators.required]
    });
  }

  onSubmit(): void {
    if (this.loginForm.valid) {
      this.authService.login(this.loginForm.value).subscribe({
        next: (token) => {
          console.log('Login exitoso');
          this.router.navigate(['/dashboard']);
        },
        error: (error) => {
          this.errorMessage = 'Credenciales inválidas';
          console.error('Error en login:', error);
        }
      });
    }
  }
}
```

### Ejemplo de Uso con Fetch API (Vanilla JS)

```javascript
// Login
async function login(username, password) {
  try {
    const response = await fetch('http://localhost:8080/api/auth/login', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ username, password })
    });
    
    if (!response.ok) {
      throw new Error('Login failed');
    }
    
    const token = await response.text();
    localStorage.setItem('token', token);
    return token;
  } catch (error) {
    console.error('Error:', error);
    throw error;
  }
}

// Obtener moteles
async function getMotels() {
  try {
    const response = await fetch('http://localhost:8080/api/motels');
    
    if (!response.ok) {
      throw new Error('Failed to fetch motels');
    }
    
    const motels = await response.json();
    return motels;
  } catch (error) {
    console.error('Error:', error);
    throw error;
  }
}

// Crear reserva (requiere autenticación)
async function createReservation(reservationData) {
  const token = localStorage.getItem('token');
  
  try {
    const response = await fetch('http://localhost:8080/api/reservations', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify(reservationData)
    });
    
    if (!response.ok) {
      throw new Error('Failed to create reservation');
    }
    
    const reservation = await response.json();
    return reservation;
  } catch (error) {
    console.error('Error:', error);
    throw error;
  }
}
```

---

## 🔧 Configuración en app.module.ts

Para usar los servicios e interceptor en Angular:

```typescript
import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { ReactiveFormsModule } from '@angular/forms';

import { AppComponent } from './app.component';
import { JwtInterceptor } from './interceptors/jwt.interceptor';
import { AuthService } from './services/auth.service';
import { MotelService } from './services/motel.service';
import { ReservationService } from './services/reservation.service';

@NgModule({
  declarations: [
    AppComponent,
    // ... tus componentes
  ],
  imports: [
    BrowserModule,
    HttpClientModule,
    ReactiveFormsModule,
    // ... otros módulos
  ],
  providers: [
    AuthService,
    MotelService,
    ReservationService,
    {
      provide: HTTP_INTERCEPTORS,
      useClass: JwtInterceptor,
      multi: true
    }
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
```

---

## 📝 Notas Adicionales

### Buenas Prácticas

1. **Siempre validar tokens JWT**: El token puede expirar, implementa lógica para renovar o redirigir al login
2. **Manejo de errores**: Implementa manejo robusto de errores en cada petición
3. **Loading states**: Muestra indicadores de carga durante peticiones HTTP
4. **Caché local**: Considera implementar caché para datos que no cambian frecuentemente
5. **Validación de formularios**: Usa la misma validación en frontend que en backend

### Seguridad

1. **Nunca expongas el JWT_SECRET**: Es solo para el backend
2. **HTTPS en producción**: Siempre usa HTTPS en producción
3. **Sanitización de inputs**: Sanitiza todos los inputs del usuario
4. **Protección CSRF**: Aunque JWT mitiga CSRF, considera protecciones adicionales
5. **Rate limiting**: Considera implementar rate limiting en el gateway

### Testing

```bash
# Verificar que los servicios están corriendo
curl http://localhost:8080/actuator/health

# Test de login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"test123"}'

# Test de endpoint con JWT
curl http://localhost:8080/api/user \
  -H "Authorization: Bearer <tu_token_jwt>"
```

---

## 🆘 Solución de Problemas

### Error: CORS

Si recibes errores CORS:
- Verifica que el gateway esté corriendo
- Asegúrate de estar usando `http://localhost:8080` como base URL
- El gateway tiene CORS configurado para permitir todos los orígenes en desarrollo

### Error: 401 Unauthorized

- Verifica que el token JWT sea válido
- Revisa que el header `Authorization` tenga el formato correcto: `Bearer <token>`
- El token puede haber expirado (duración por defecto: 24 horas)

### Error: 404 Not Found

- Verifica que la ruta sea correcta (todas empiezan con `/api/`)
- Asegúrate de que el microservicio correspondiente esté corriendo
- Revisa los logs del gateway para ver si la ruta está configurada

### Error: Connection Refused

- Verifica que todos los microservicios estén corriendo
- Revisa los puertos: Gateway(8080), UserManagement(8081), Products(8082), MotelManagement(8083)
- Verifica que las bases de datos (PostgreSQL/MySQL) estén corriendo

---

## 📚 Referencias

- [Documentación Spring Cloud Gateway](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/)
- [Documentación Spring WebFlux](https://docs.spring.io/spring-framework/reference/web/webflux.html)
- [Angular HttpClient](https://angular.io/guide/http)
- [JWT.io](https://jwt.io/)

---

**Última actualización**: Diciembre 2024  
**Versión de la API**: 1.0  
**Mantenido por**: Equipo Ubik App
