import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../../services/auth.service';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
type UserRole = 'CLIENT' | 'PROVIDER';
@Component({
  selector: 'app-signup',
  imports: [MatIconModule, ReactiveFormsModule, RouterModule, CommonModule],
  templateUrl: './signup.component.html',
  styleUrl: './signup.component.scss',
})
export class SignupComponent {
  private fb = inject(FormBuilder);
  private router = inject(Router);
  private authService = inject(AuthService);

  isLoading = signal<boolean>(false);
  errorMessage = signal<string | null>(null);

  selectedRole = signal<UserRole>('PROVIDER');

  signupForm = this.fb.nonNullable.group({
    fullName: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
    phone: [''],

    professionalTitle: ['', Validators.required],
    bio: [''],
    experienceYears: [0]
  });

  toggleRole(role: UserRole): void {
    this.selectedRole.set(role);

    const titleControl = this.signupForm.get('professionalTitle');

    if (role === 'PROVIDER') {
      titleControl?.setValidators([Validators.required]);
    } else {
      titleControl?.clearValidators();
      this.signupForm.patchValue({ professionalTitle: '', bio: '', experienceYears: 0 });
    }
    titleControl?.updateValueAndValidity();
  }

  onSubmit(): void {
    if (this.signupForm.invalid) {
      this.signupForm.markAllAsTouched();
      return;
    }

    this.isLoading.set(true);
    this.errorMessage.set(null);

    const formData = {
      ...this.signupForm.getRawValue(),
      role: this.selectedRole()
    };

    console.log('Enviando para o Spring Boot:', formData);

    // Simulação de chamada HTTP
    setTimeout(() => {
      this.isLoading.set(false);
      // Aqui entraria o this.authService.register(formData).subscribe(...)
      // this.router.navigate(['/auth/login']);
    }, 1500);
  }
}
