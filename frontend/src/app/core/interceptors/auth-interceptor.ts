import { HttpInterceptorFn } from '@angular/core';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

/**
 * Interceptor HTTP funcional para manejar autenticación
 * Agrega automáticamente el token de autorización a todas las peticiones HTTP
 * y maneja errores de autenticación
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);

  // Obtener el token del localStorage
  const token = localStorage.getItem('authToken');

  // Si existe el token, clonamos la petición y agregamos el header de autorización
  let authReq = req;
  if (token) {
    authReq = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  // Continuar con la petición y manejar errores
  return next(authReq).pipe(
    catchError((error) => {
      // Si el error es 401 (No Autorizado), redirigir al login
      if (error.status === 401) {
        localStorage.removeItem('authToken');
        localStorage.removeItem('user');
        router.navigate(['/login']);
      }

      // Re-lanzar el error para que los componentes lo puedan manejar
      return throwError(() => error);
    })
  );
};