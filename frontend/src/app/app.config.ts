import { ApplicationConfig } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { routes } from './app.routes';
import { authInterceptor } from '../app/core/interceptors/auth-interceptor';

/**
 * Configuración principal de la aplicación Angular
 * Registra los providers necesarios:
 * - Router con las rutas de la aplicación
 * - HttpClient con el interceptor de autenticación
 */
export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideHttpClient(
      withInterceptors([authInterceptor])
    )
  ]
};