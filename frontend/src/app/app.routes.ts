import { Routes } from '@angular/router';
import { authGuard, adminGuard } from './core/guards/auth.guard';

/**
 * Configuración de rutas de la aplicación
 * Incluye protección con guards para rutas que requieren autenticación
 */
export const routes: Routes = [
  {
    path: '',
    redirectTo: 'home',
    pathMatch: 'full'
  },
  {
    path: 'home',
    loadComponent: () => import('./views/home/home').then(m => m.Home)
  },
  {
    path: 'login',
    loadComponent: () => import('./views/user-profile/services/').then(m => m.Login)
  },
  {
    path: 'register',
    loadComponent: () => import('./pages/register/register').then(m => m.Register)
  },
  {
    path: 'admin',
    loadComponent: () => import('./pages/admin-dashboard/admin-dashboard').then(m => m.AdminDashboard),
    canActivate: [authGuard, adminGuard] // Requiere autenticación y rol de admin
  },
  {
    path: 'profile',
    loadComponent: () => import('./pages/profile/profile').then(m => m.Profile),
    canActivate: [authGuard] // Requiere autenticación
  },
  {
    path: '**',
    redirectTo: 'home'
  }
];