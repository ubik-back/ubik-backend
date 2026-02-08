import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EstablishmentLocation } from './establishment-location';

describe('EstablishmentLocation', () => {
  let component: EstablishmentLocation;
  let fixture: ComponentFixture<EstablishmentLocation>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EstablishmentLocation]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EstablishmentLocation);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
