import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EstablishmentInfo } from './establishment-info';

describe('EstablishmentInfo', () => {
  let component: EstablishmentInfo;
  let fixture: ComponentFixture<EstablishmentInfo>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EstablishmentInfo]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EstablishmentInfo);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
