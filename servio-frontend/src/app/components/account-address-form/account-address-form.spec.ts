import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AccountAddressForm } from './account-address-form';

describe('AccountAddressForm', () => {
  let component: AccountAddressForm;
  let fixture: ComponentFixture<AccountAddressForm>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AccountAddressForm]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AccountAddressForm);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
