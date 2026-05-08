import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-account-contact-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './account-contact-form.html',
  styleUrl: './account-contact-form.scss',
})
export class AccountContactFormComponent {
  @Input({ required: true }) contact!: {
    email: string;
    phone: string;
  };

  phoneTouched = false;

  onPhoneChange(value: string): void {
    const digits = (value ?? '').replace(/\D/g, '').slice(0, 11);

    if (digits.length <= 2) {
      this.contact.phone = digits;
      return;
    }

    if (digits.length <= 6) {
      this.contact.phone = `(${digits.slice(0, 2)}) ${digits.slice(2)}`;
      return;
    }

    if (digits.length <= 10) {
      this.contact.phone = `(${digits.slice(0, 2)}) ${digits.slice(2, 6)}-${digits.slice(6)}`;
      return;
    }

    this.contact.phone = `(${digits.slice(0, 2)}) ${digits.slice(2, 7)}-${digits.slice(7)}`;
  }

  onPhoneBlur(): void {
    this.phoneTouched = true;
  }

  get phoneInvalid(): boolean {
    const digits = (this.contact.phone ?? '').replace(/\D/g, '');
    return this.phoneTouched && digits.length > 0 && digits.length < 10;
  }
}
