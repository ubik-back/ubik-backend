import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService, MotelApproval, ApprovalStatistics } from '../../core/services/admin/admin.service';

/**
 * Vista del panel de administración
 * Gestiona la aprobación/rechazo de moteles pendientes
 */
@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-dashboard.html',
  styleUrl: './admin-dashboard.css'
})
export class AdminDashboard implements OnInit {

  // Señales reactivas
  pendingMotels = signal<MotelApproval[]>([]);
  statistics = signal<ApprovalStatistics | null>(null);
  selectedMotel = signal<MotelApproval | null>(null);
  currentFilter = signal<string>('PENDING');
  isLoading = signal(false);
  rejectionReason = signal('');
  showRejectModal = signal(false);
  showDetailsModal = signal(false);
  currentImageIndex = signal(0);
  error = signal<string | null>(null);

  constructor(private adminService: AdminService) {}

  ngOnInit(): void {
    this.loadStatistics();
    this.loadPendingMotels();
  }

  /**
   * Carga las estadísticas de aprobación
   */
  loadStatistics(): void {
    this.adminService.getApprovalStatistics().subscribe({
      next: (stats) => {
        this.statistics.set(stats);
        this.error.set(null);
      },
      error: (err) => {
        console.error('Error cargando estadísticas', err);
        this.error.set('Error al cargar las estadísticas');
      }
    });
  }

  /**
   * Carga los moteles pendientes de aprobación
   */
  loadPendingMotels(): void {
    this.isLoading.set(true);
    this.error.set(null);

    this.adminService.getPendingMotels().subscribe({
      next: (motels) => {
        this.pendingMotels.set(motels);
        this.isLoading.set(false);
      },
      error: (err) => {
        console.error('Error cargando moteles pendientes', err);
        this.error.set('Error al cargar los moteles. Por favor, verifica tu sesión.');
        this.isLoading.set(false);
      }
    });
  }

  /**
   * Cambia el filtro de visualización
   */
  changeFilter(status: string): void {
    this.currentFilter.set(status);
    this.isLoading.set(true);
    this.error.set(null);

    this.adminService.getMotelsByStatus(status).subscribe({
      next: (motels) => {
        this.pendingMotels.set(motels);
        this.isLoading.set(false);
      },
      error: (err) => {
        console.error('Error filtrando moteles', err);
        this.error.set('Error al filtrar los moteles');
        this.isLoading.set(false);
      }
    });
  }

  /**
   * Selecciona un motel para ver detalles
   */
  selectMotel(motel: MotelApproval): void {
    this.selectedMotel.set(motel);
    this.showDetailsModal.set(true);
    this.currentImageIndex.set(0);
  }

  /**
   * Cierra el panel de detalles
   */
  closeDetails(): void {
    this.selectedMotel.set(null);
    this.showDetailsModal.set(false);
    this.currentImageIndex.set(0);
  }

  /**
   * Navega a la siguiente imagen
   */
  nextImage(): void {
    const motel = this.selectedMotel();
    if (motel && motel.imageUrls && motel.imageUrls.length > 0) {
      const currentIndex = this.currentImageIndex();
      const nextIndex = (currentIndex + 1) % motel.imageUrls.length;
      this.currentImageIndex.set(nextIndex);
    }
  }

  /**
   * Navega a la imagen anterior
   */
  previousImage(): void {
    const motel = this.selectedMotel();
    if (motel && motel.imageUrls && motel.imageUrls.length > 0) {
      const currentIndex = this.currentImageIndex();
      const prevIndex = currentIndex === 0 ? motel.imageUrls.length - 1 : currentIndex - 1;
      this.currentImageIndex.set(prevIndex);
    }
  }

  /**
   * Aprueba un motel
   */
  approveMotel(motelId: number, event?: Event): void {
    if (event) {
      event.stopPropagation();
    }

    if (!confirm('¿Estás seguro de aprobar este motel?')) {
      return;
    }

    this.adminService.approveMotel(motelId).subscribe({
      next: (response) => {
        console.log('Motel aprobado:', response);
        alert('Motel aprobado exitosamente');
        this.loadPendingMotels();
        this.loadStatistics();
        this.closeDetails();
        this.error.set(null);
      },
      error: (err) => {
        console.error('Error aprobando motel', err);
        alert('Error al aprobar el motel. Por favor, intenta de nuevo.');
        this.error.set('Error al aprobar el motel');
      }
    });
  }

  /**
   * Abre el modal para rechazar un motel
   */
  openRejectModal(motelId: number, event?: Event): void {
    if (event) {
      event.stopPropagation();
    }

    const motel = this.pendingMotels().find(m => m.id === motelId);
    if (motel) {
      this.selectedMotel.set(motel);
      this.showRejectModal.set(true);
      this.showDetailsModal.set(false);
    }
  }

  /**
   * Rechaza un motel con una razón
   */
  rejectMotel(): void {
    const motel = this.selectedMotel();
    const reason = this.rejectionReason().trim();

    if (!motel || !reason) {
      alert('Debes proporcionar una razón para el rechazo');
      return;
    }

    this.adminService.rejectMotel(motel.id, reason).subscribe({
      next: (response) => {
        console.log('Motel rechazado:', response);
        alert('Motel rechazado exitosamente');
        this.loadPendingMotels();
        this.loadStatistics();
        this.closeRejectModal();
        this.error.set(null);
      },
      error: (err) => {
        console.error('Error rechazando motel', err);
        alert('Error al rechazar el motel. Por favor, intenta de nuevo.');
        this.error.set('Error al rechazar el motel');
      }
    });
  }

  /**
   * Cierra el modal de rechazo
   */
  closeRejectModal(): void {
    this.showRejectModal.set(false);
    this.rejectionReason.set('');
    this.selectedMotel.set(null);
  }

  /**
   * Pone un motel en revisión
   */
  putUnderReview(motelId: number, event?: Event): void {
    if (event) {
      event.stopPropagation();
    }

    this.adminService.putMotelUnderReview(motelId).subscribe({
      next: (response) => {
        console.log('Motel puesto en revisión:', response);
        alert('Motel puesto en revisión exitosamente');
        this.loadPendingMotels();
        this.loadStatistics();
        this.error.set(null);
      },
      error: (err) => {
        console.error('Error poniendo motel en revisión', err);
        alert('Error al poner el motel en revisión. Por favor, intenta de nuevo.');
        this.error.set('Error al poner el motel en revisión');
      }
    });
  }

  /**
   * Obtiene el color para el badge de estado
   */
  getStatusColor(status: string): string {
    const colors: Record<string, string> = {
      'PENDING': 'bg-yellow-500',
      'UNDER_REVIEW': 'bg-blue-500',
      'APPROVED': 'bg-green-500',
      'REJECTED': 'bg-red-500'
    };
    return colors[status] || 'bg-gray-500';
  }

  /**
   * Obtiene el texto traducido del estado
   */
  getStatusText(status: string): string {
    const texts: Record<string, string> = {
      'PENDING': 'Pendiente',
      'UNDER_REVIEW': 'En Revisión',
      'APPROVED': 'Aprobado',
      'REJECTED': 'Rechazado'
    };
    return texts[status] || status;
  }

  /**
   * Verifica si la información legal está completa
   */
  hasCompleteLegalInfo(motel: MotelApproval): boolean {
    return !!(
      motel.rues &&
      motel.rnt &&
      motel.ownerDocumentType &&
      motel.ownerDocumentNumber &&
      motel.ownerFullName
    );
  }
}