import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { Button01 } from '../../../../../components/button-01/button-01';
import { Inputcomponent } from '../../../../../components/input/input';
import { RegisterService } from '../services/services';
import { ValidationError } from '../types/register-user.types';
import { RegisterUserPayload } from '../types/register-user-payload.types';
import { RegisterFormData } from '../types/register-user.types';

@Component({
  selector: 'app-register-user',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule,Inputcomponent],
  templateUrl: './register-user.html',
  styleUrl: './register-user.css',
})
export class RegisterUser implements OnInit {
  registerForm: FormGroup;
  validationErrors: ValidationError[] = [];
  isSubmitting = false;
  progress = 0;

  constructor(
    private fb: FormBuilder,
    private registerService: RegisterService,
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
    this.registerForm.valueChanges.subscribe(() => {
      this.updateProgress();
    });
  }

  /** Progreso del formulario */
  updateProgress(): void {
    const values = this.registerForm.value;
    let completed = 0;
    const totalFields = 6; // ✅ FIX

    if (values.username?.trim()) completed++;
    if (values.email?.trim()) completed++;
    if (values.phoneNumber?.trim()) completed++;
    if (values.birthDate) completed++;
    if (values.password?.trim()) completed++;
    if (values.confirmPassword?.trim()) completed++; // ✅ FIX

    this.progress = Math.round((completed / totalFields) * 100);
  }

  /** Obtener error por campo */
  getFieldError(fieldName: string): string | null {
    const error = this.validationErrors.find(e => e.field === fieldName);
    return error ? error.message : null;
  }

  /** Submit */
  onSubmit(): void {
    if (this.isSubmitting) return;
    this.validationErrors = [];

    const form = this.registerForm.value;

    // 🚫 Mayor de edad
    if (!this.isAdult(form.birthDate)) {
      this.validationErrors.push({
        field: 'birthDate',
        message: 'Debes ser mayor de 18 años',
      });
      return;
    }

    const payload = {
      username: form.username?.trim(),
      email: form.email?.trim(),
      password: form.password,
      comfirmPassword: form.comfirmPassword,
      phoneNumber: form.phoneNumber?.trim(),
      anonymous: false,
      roleId: 3,
      birthDate: form.birthDate,
      latitude: 4.6097,
      longitude: -74.0721
    };

    console.log('🚀 PAYLOAD', payload);

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
  clearFieldError(fieldName: string): void {
    this.validationErrors = this.validationErrors.filter(e => e.field !== fieldName);
  }

  /** Validar +18 */
  isAdult(birthDateString: string): boolean {
    if (!birthDateString) return false;

    const birthDate = new Date(birthDateString);
    const today = new Date();

    let age = today.getFullYear() - birthDate.getFullYear();
    const monthDiff = today.getMonth() - birthDate.getMonth();

    if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDate.getDate())) {
      age--;
    }

    return age >= 18;
  }
}
