import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DashboardProviderComponent } from './dashboard-provider.component';

describe('DashboardProviderComponent', () => {
  let component: DashboardProviderComponent;
  let fixture: ComponentFixture<DashboardProviderComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DashboardProviderComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DashboardProviderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
