import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ModalTaxonomyComponent } from './modal-taxonomy.component';

describe('ModalTaxonomyComponent', () => {
  let component: ModalTaxonomyComponent;
  let fixture: ComponentFixture<ModalTaxonomyComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ModalTaxonomyComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ModalTaxonomyComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
