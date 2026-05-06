import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AddExtraSlotModalComponent } from './add-extra-slot-modal.component';

describe('AddExtraSlotModalComponent', () => {
  let component: AddExtraSlotModalComponent;
  let fixture: ComponentFixture<AddExtraSlotModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AddExtraSlotModalComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AddExtraSlotModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
