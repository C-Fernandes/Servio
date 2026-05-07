import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OrderKanbanCardComponent } from './order-kanban-card.component';

describe('OrderKanbanCardComponent', () => {
  let component: OrderKanbanCardComponent;
  let fixture: ComponentFixture<OrderKanbanCardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [OrderKanbanCardComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(OrderKanbanCardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
