import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TodosLosComponentes } from './todos-los-componentes';

describe('TodosLosComponentes', () => {
  let component: TodosLosComponentes;
  let fixture: ComponentFixture<TodosLosComponentes>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TodosLosComponentes]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TodosLosComponentes);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
