import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ThreeButtom } from './three-buttom';

describe('ThreeButtom', () => {
  let component: ThreeButtom;
  let fixture: ComponentFixture<ThreeButtom>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ThreeButtom]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ThreeButtom);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
