import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-account-personal-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './account-personal-form.html',
  styleUrl: './account-personal-form.scss',
})
export class AccountPersonalFormComponent {
  @Input({ required: true }) personal!: {
    fullName: string;
  };
}
