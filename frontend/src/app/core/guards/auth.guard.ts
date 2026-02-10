import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { CanActivateFn } from '@angular/router';

/**
 * Guard funcional para proteger rutas que requieren autenticación
 * Verifica si existe un token válido en localStorage
 * Si no existe, redirige al login
 */
export const authGuard: CanActivateFn = (route, state) => {
  const router = inject(Router);
  const token = localStorage.getItem('authToken');

  if (token) {
    return true;
  }

  // Redirigir al login guardando la URL de destino
  router.navigate(['/login'], {
    queryParams: { returnUrl: state.url }
  });
  return false;
};

/**
 * Guard funcional para proteger la ruta de admin
 * Verifica si el usuario tiene rol de administrador
 */
export const adminGuard: CanActivateFn = (route, state) => {
  const router = inject(Router);
  const userStr = localStorage.getItem('user');

  if (!userStr) {
    router.navigate(['/login']);
    return false;
  }

  try {
    const user = JSON.parse(userStr);
    
    // Verificar si el usuario tiene rol de admin
    if (user.role === 'ADMIN' || user.isAdmin === true) {
      return true;
    }

    // Si no es admin, redirigir a la página principal
    router.navigate(['/']);
    alert('No tienes permisos para acceder a esta sección');
    return false;
  } catch (error) {
    console.error('Error parsing user data:', error);
    router.navigate(['/login']);
    return false;
  }
};