import { TestBed } from '@angular/core/testing';

import { FinancialDashboardService } from './financial-dashboard.service';

describe('FinancialDashboardService', () => {
  let service: FinancialDashboardService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(FinancialDashboardService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
