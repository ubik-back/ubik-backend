import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EstablishmentConfirm } from './establishment-confirm';

describe('EstablishmentConfirm', () => {
  let component: EstablishmentConfirm;
  let fixture: ComponentFixture<EstablishmentConfirm>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EstablishmentConfirm]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EstablishmentConfirm);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
