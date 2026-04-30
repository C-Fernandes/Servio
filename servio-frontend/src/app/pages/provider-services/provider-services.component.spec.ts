import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ProviderServicesComponent } from './provider-services.component';

describe('ProviderServicesComponent', () => {
  let component: ProviderServicesComponent;
  let fixture: ComponentFixture<ProviderServicesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProviderServicesComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ProviderServicesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
