import { HttpInterceptorFn } from '@angular/common/http';
import { inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';

const PUBLIC_ENDPOINTS = [
  '/auth/login',
  '/auth/register',
];

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const platformId = inject(PLATFORM_ID);

  // SSR: no tocar storage
  if (!isPlatformBrowser(platformId)) {
    return next(req);
  }

  // Endpoints públicos
  if (PUBLIC_ENDPOINTS.some(url => req.url.includes(url))) {
    return next(req);
  }

  const token =
    localStorage.getItem('auth_token') ??
    sessionStorage.getItem('auth_token');

  if (!token) {
    return next(req);
  }

  const authReq = req.clone({
    setHeaders: {
      Authorization: `Bearer ${token}`,
    },
  });

  return next(authReq);
};