import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AccountPersonalForm } from './account-personal-form';

describe('AccountPersonalForm', () => {
  let component: AccountPersonalForm;
  let fixture: ComponentFixture<AccountPersonalForm>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AccountPersonalForm]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AccountPersonalForm);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
