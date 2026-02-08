import { Component, computed, signal } from '@angular/core';
import { Router, NavigationEnd, RouterLink } from '@angular/router';
import { filter } from 'rxjs';
import { Logo01 } from "../../components/logo-01/logo-01";
import { Button01 } from "../../components/button-01/button-01";

@Component({
  selector: 'app-header',
  imports: [Logo01, RouterLink, Button01],
  templateUrl: './header.html',
})
export class Header {

  title = 'ENCUENTRA EL LUGAR PERFECTO PARA TU MOMENTO ESPECIAL';
  subtitle = 'Descubre moteles y espacios únicos cerca de ti, de forma rápida y segura.';

  currentUrl = signal<string>('/');

  showSearch = computed(() => {
    return this.currentUrl() === '/';
  });

  // constructor(private router: Router) {
  //   this.currentUrl.set(this.normalizeUrl(this.router.url));

  //   this.router.events
  //     .pipe(filter(e => e instanceof NavigationEnd))
  //     .subscribe((e: any) => {
  //       this.currentUrl.set(this.normalizeUrl(e.urlAfterRedirects));
  //     });
  // }

  constructor(private router: Router) {
    // URL inicial limpia (sin #hash)
    this.currentUrl.set(this.router.url.split('#')[0] || '/');

    // Escuchar cambios de ruta
    this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe(() => {
        this.currentUrl.set(this.router.url.split('#')[0] || '/');
      });
  }

  // private normalizeUrl(url: string): string {
  //   let clean = url.split('?')[0].replace(/\/$/, '');
  //   clean = clean.split('#')[0];
  //   return clean === '' ? '/' : clean;
  // }
}
