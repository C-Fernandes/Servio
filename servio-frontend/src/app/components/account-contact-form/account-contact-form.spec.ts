import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AccountContactForm } from './account-contact-form';

describe('AccountContactForm', () => {
  let component: AccountContactForm;
  let fixture: ComponentFixture<AccountContactForm>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AccountContactForm]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AccountContactForm);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
