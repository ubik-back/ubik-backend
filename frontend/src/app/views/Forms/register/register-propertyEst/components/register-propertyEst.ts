import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { Inputcomponent } from '../../../../../components/input/input';
import { RegisterServiceOwner } from '../services/services';
import { ValidationError } from '../../register-user/types/register-user.types';
import { RegisterFormData } from '../../register-user/types/register-user.types';

@Component({
  selector: 'app-register-propertyEst',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule,Inputcomponent],
  templateUrl: './register-propertyEst.html',
  styleUrl: './register-propertyEst.css',
})
export class RegisterPropertyEst implements OnInit {
  registerForm: FormGroup;
  validationErrors: ValidationError[] = [];
  isSubmitting = false;
  progress = 10;

  constructor(
    private fb: FormBuilder,
    private registerService: RegisterServiceOwner,
    private router: Router
  ) {
    this.registerForm = this.fb.group({
      username: [''],
      email: [''],
      phoneNumber: [''],
      birthDate: [''],
      password: [''],
      comfirmPassword: [''],
      anonymous: [false]
    });
  }

  ngOnInit(): void {
    // Calcular progreso basado en campos completados
    this.registerForm.valueChanges.subscribe(() => {
      this.updateProgress();
    });
  }

  /**
   * Actualiza la barra de progreso según campos completados
   */
  updateProgress(): void {
    const v = this.registerForm.value;
    let completed = 0;
    const total = 6;

    if (v.username?.trim()) completed++;
    if (v.email?.trim()) completed++;
    if (v.phoneNumber?.trim()) completed++;
    if (v.birthDate) completed++;
    if (v.password?.trim()) completed++;
    if (v.comfirmPassword?.trim()) completed++;

    this.progress = Math.round((completed / total) * 100);
  }

  /**
   * Obtiene error para un campo específico
   */
  getFieldError(fieldName: string): string | null {
    const error = this.validationErrors.find(e => e.field === fieldName);
    return error ? error.message : null;
  }

  /**
   * Convierte fecha de input date a día, mes, año separados
   */
  parseBirthDate(dateString: string): { day: string; month: string; year: string } {
    if (!dateString) return { day: '', month: '', year: '' };
    
    const date = new Date(dateString);
    return {
      day: String(date.getDate()).padStart(2, '0'),
      month: String(date.getMonth() + 1).padStart(2, '0'),
      year: String(date.getFullYear())
    };
  }

  /**
   * Maneja el envío del formulario
   */
  onSubmit(): void {

    if (this.isSubmitting) return;
    this.validationErrors = [];

    const form = this.registerForm.value;

    // 🔞 Validar mayoría de edad
    if (!this.isAdult(form.birthDate)) {
      this.validationErrors.push({
        field: 'birthDate',
        message: 'Debes ser mayor de 18 años',
      });
      return;
    }

    const payload: RegisterFormData = {
      username: form.username?.trim(),
      email: form.email?.trim(),
      password: form.password,
      comfirmPassword: form.comfirmPassword, 
      phoneNumber: form.phoneNumber?.trim(),
      birthDate: form.birthDate,
      anonymous: form.anonymous ?? false,
      roleId: 2,
      latitude: 4.6097,
      longitude: -74.0721
    };

    // ✅ Validación frontend
    const errors = this.registerService.validateClientForm(payload);
    if (errors.length) {
      this.validationErrors = errors;
      return;
    }

    this.isSubmitting = true;

    this.registerService.submitClientRegistration(payload).subscribe({
      next: () => {
        console.log('🟢 REGISTER OK');
        this.router.navigate(['/login']);
      },
      error: err => {
        console.error('🔴 REGISTER ERROR', err);
        this.isSubmitting = false;
      }
    });
  }

  /** Limpiar error */
  clearFieldError(field: string): void {
    this.validationErrors = this.validationErrors.filter(e => e.field !== field);
  }

  /** Validar +18 */
  isAdult(dateStr: string): boolean {
    if (!dateStr) return false;

    const birth = new Date(dateStr);
    const today = new Date();

    let age = today.getFullYear() - birth.getFullYear();
    const m = today.getMonth() - birth.getMonth();

    if (m < 0 || (m === 0 && today.getDate() < birth.getDate())) age--;

    return age >= 18;
  }
}