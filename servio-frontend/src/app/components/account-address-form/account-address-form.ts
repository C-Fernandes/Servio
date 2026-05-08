import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-account-address-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './account-address-form.html',
  styleUrl: './account-address-form.scss',
})
export class AccountAddressFormComponent {
  @Input({ required: true }) address!: {
    zipCode: string;
    street: string;
    number: string;
    complement: string;
    district: string;
    city: string;
    state: string;
  };

  limitText(value: string | null | undefined, max: number): string {
    return (value ?? '').slice(0, max);
  }

  onZipCodeChange(value: string): void {
    const digits = (value ?? '').replace(/\D/g, '').slice(0, 8);

    if (digits.length <= 5) {
      this.address.zipCode = digits;
      return;
    }

    this.address.zipCode = `${digits.slice(0, 5)}-${digits.slice(5)}`;
  }

  onNumberChange(value: string): void {
    this.address.number = (value ?? '').replace(/\D/g, '').slice(0, 10);
  }

  onStateChange(value: string): void {
    this.address.state = (value ?? '')
      .replace(/[^a-zA-Z]/g, '')
      .toUpperCase()
      .slice(0, 2);
  }
}
