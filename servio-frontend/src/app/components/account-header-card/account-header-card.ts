import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-account-header-card',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './account-header-card.html',
  styleUrl: './account-header-card.scss',
})
export class AccountHeaderCardComponent {
  @Input() fullName = '';
  @Input() email = '';

  get initials(): string {
    if (!this.fullName?.trim()) return 'U';

    const parts = this.fullName.trim().split(' ');
    const first = parts[0]?.charAt(0) ?? '';
    const second = parts[1]?.charAt(0) ?? '';

    return (first + second).toUpperCase();
  }
}
