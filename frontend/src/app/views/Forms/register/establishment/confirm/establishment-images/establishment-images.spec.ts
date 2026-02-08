import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EstablishmentImages } from './establishment-images';

describe('EstablishmentImages', () => {
  let component: EstablishmentImages;
  let fixture: ComponentFixture<EstablishmentImages>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EstablishmentImages]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EstablishmentImages);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
