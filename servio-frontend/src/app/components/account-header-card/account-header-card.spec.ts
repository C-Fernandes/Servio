import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AccountHeaderCard } from './account-header-card';

describe('AccountHeaderCard', () => {
  let component: AccountHeaderCard;
  let fixture: ComponentFixture<AccountHeaderCard>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AccountHeaderCard]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AccountHeaderCard);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
