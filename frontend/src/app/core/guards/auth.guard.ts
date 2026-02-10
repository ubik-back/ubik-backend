import { inject } from '@angular/core';
import { Router, type CanActivateFn } from '@angular/router';
import { AuthService } from '../auth.service';

/**
 * Guard para proteger rutas que requieren autenticación de administrador
 * Solo permite acceso a usuarios con roleId = 1 (Admin)
 */
export const adminGuard: CanActivateFn = (route, state) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  // Verificar si el usuario está logueado
  if (!auth.isLogged()) {
    console.warn('⛔ Acceso denegado: Usuario no autenticado');
    router.navigate(['/login']);
    return false;
  }

  // Verificar si el usuario es administrador
  if (!auth.isAdmin()) {
    console.warn('⛔ Acceso denegado: Usuario no es administrador');
    router.navigate(['/']);
    return false;
  }

  return true;
};

/**
 * Guard para proteger rutas que requieren autenticación de propietario
 * Solo permite acceso a usuarios con roleId = 2 (Owner)
 */
export const ownerGuard: CanActivateFn = (route, state) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (!auth.isLogged()) {
    console.warn('⛔ Acceso denegado: Usuario no autenticado');
    router.navigate(['/login']);
    return false;
  }

  if (!auth.isOwner()) {
    console.warn('⛔ Acceso denegado: Usuario no es propietario');
    router.navigate(['/']);
    return false;
  }

  return true;
};

/**
 * Guard para proteger rutas que requieren autenticación básica
 * Permite acceso a cualquier usuario autenticado
 */
export const authGuard: CanActivateFn = (route, state) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (!auth.isLogged()) {
    console.warn('⛔ Acceso denegado: Usuario no autenticado');
    router.navigate(['/login']);
    return false;
  }

  return true;
};