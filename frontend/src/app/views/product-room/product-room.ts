import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { MotelMockService, Room } from '../../core/services/motel/motel-mock';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-product',
  imports: [CommonModule, RouterModule], 
  templateUrl: './product-room.html',
})
export class ProductComponent implements OnInit {
  product: Room | null = null;
  selectedImage: string = '';
  currentImageIndex: number = 0;
  loading: boolean = true;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private motelService: MotelMockService
  ) {}

  ngOnInit(): void {
    const productId = this.route.snapshot.paramMap.get('id');
    if (productId) {
      this.loadProduct(productId);
    } else {
      this.loading = false;
    }
  }

  loadProduct(id: string): void {
    this.loading = true;
    const roomId = parseInt(id, 10);
    
    this.motelService.getRoomById(roomId).subscribe({
      next: (room) => {
        this.product = room || null;
        
        // Seleccionar primera imagen disponible
        if (room && room.photos && room.photos.length > 0) {
          this.selectedImage = room.photos[0].url;
        }
        
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading product:', error);
        this.loading = false;
        this.product = null;
      }
    });
  }

  selectImage(image: string, index: number): void {
    this.selectedImage = image;
    this.currentImageIndex = index;
  }

  toggleFavorite(): void {
    if (!this.product) return;
    console.log('Toggle favorite for room:', this.product.id);
    // TODO: Implementar lógica de favoritos
  }

  makeReservation(): void {
    if (!this.product) return;
    console.log('Making reservation for room:', this.product.id);
    // TODO: Navegar a página de reserva
    alert(`Reservando: ${this.product.room_type} ${this.product.num_or_name}\nPrecio: $${this.product.price.toLocaleString()}`);
  }

  showMoreInfo(): void {
    if (!this.product) return;
    console.log('Show more info');
    // TODO: Mostrar más información
  }

  goBack(): void {
    this.router.navigate(['/']);
  }

  formatPrice(price: number): string {
    return new Intl.NumberFormat('es-CO', {
      style: 'currency',
      currency: 'COP',
      minimumFractionDigits: 0
    }).format(price);
  }
}