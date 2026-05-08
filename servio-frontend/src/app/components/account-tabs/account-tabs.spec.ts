import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AccountTabs } from './account-tabs';

describe('AccountTabs', () => {
  let component: AccountTabs;
  let fixture: ComponentFixture<AccountTabs>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AccountTabs]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AccountTabs);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
