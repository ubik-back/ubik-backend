import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NavBarBottom } from './nav-bar-bottom';

describe('NavBarBottom', () => {
  let component: NavBarBottom;
  let fixture: ComponentFixture<NavBarBottom>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NavBarBottom]
    })
    .compileComponents();

    fixture = TestBed.createComponent(NavBarBottom);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
