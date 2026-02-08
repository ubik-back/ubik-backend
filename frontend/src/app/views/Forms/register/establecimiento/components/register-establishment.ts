import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup } from '@angular/forms';

import { EstablishmentService } from '../services/establishment.services';

import { Inputcomponent } from '../../../../../components/input/input';
import { Button01 } from '../../../../../components/button-01/button-01';

import { EstablishmentValidation } from '../utils/establishment-validation.utils';
import {
  RegisterEstablishmentForm,
  RegisterEstablishmentPayload
} from '../types/register-establishment.types';

@Component({
  selector: 'app-register-establishment',
  standalone: true,
  templateUrl: './register-establishment.html',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    Inputcomponent,
    Button01,
  ],
})
export class RegisterEstablishmentComponent implements OnInit {

  form!: FormGroup;
  images: File[] = [];
  isSubmitting = false;

  constructor(
    private fb: FormBuilder,
    private establishmentService: EstablishmentService
  ) {}

  ngOnInit(): void {
    this.buildForm();
  }

  /* ================================
   *  FORM
   * ================================ */

  private buildForm(): void {
    this.form = this.fb.group({
      name: ['', [
        EstablishmentValidation.required,
        EstablishmentValidation.minLength(3),
      ]],

      address: ['', EstablishmentValidation.required],

      city: ['', EstablishmentValidation.required],

      phoneNumber: ['', EstablishmentValidation.phoneNumber],

      description: ['', [
        EstablishmentValidation.required,
        EstablishmentValidation.minLength(10),
      ]],

      latitude: [null, EstablishmentValidation.latitude],
      longitude: [null, EstablishmentValidation.longitude],
    });
  }

  /* ================================
   *  IMÁGENES
   * ================================ */

  onImagesSelected(event: Event): void {
    const input = event.target as HTMLInputElement;

    if (!input.files || input.files.length === 0) {
      return;
    }

    this.images = Array.from(input.files);
  }

  /* ================================
   *  SUBMIT
   * ================================ */

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    if (this.images.length === 0) {
      console.warn('Debe subir al menos una imagen');
      return;
    }

    this.isSubmitting = true;

    const imageUrls = this.images.map(
      (_, index) =>
        `https://cloudinary.com/mi-cuenta/imagen_${index}.jpg`
    );

    const formValue = this.form.value as RegisterEstablishmentForm;

    const payload: RegisterEstablishmentPayload = {
      name: formValue.name,
      address: formValue.address,
      phoneNumber: formValue.phoneNumber,
      description: formValue.description,
      city: formValue.city,

      propertyId: 1,

      imageUrls,

      latitude: Number(formValue.latitude),
      longitude: Number(formValue.longitude),
    };

    this.establishmentService.createEstablishment(payload).subscribe({
      next: () => {
        this.isSubmitting = false;
        console.log('Establecimiento creado correctamente');
      },
      error: (error) => {
        this.isSubmitting = false;
        console.error('Error al crear establecimiento', error);
      },
    });
  }
}