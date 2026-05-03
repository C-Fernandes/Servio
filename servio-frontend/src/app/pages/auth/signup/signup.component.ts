import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../../services/auth/auth.service';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { ToastService } from '../../../services/toast/toast.service';
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
  private toast = inject(ToastService);

  isLoading = signal<boolean>(false);
  errorMessage = signal<string | null>(null);

  selectedRole = signal<UserRole>('CLIENT');

  signupForm = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.minLength(3)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
    bio: [''],
    experience: [0, [Validators.min(0), Validators.max(70)]]
  });

  toggleRole(role: UserRole): void {
    this.selectedRole.set(role);

    if (role === 'CLIENT') {
      this.signupForm.patchValue({ bio: '', experience: 0 });
    }
  }

  onSubmit(): void {
    if (this.signupForm.invalid) {
      this.signupForm.markAllAsTouched();
      this.toast.showToast('Por favor, preencha os campos corretamente.', 'error');
      return;
    }

    this.isLoading.set(true);


    const rawData = this.signupForm.getRawValue();
    const payload = {
      name: rawData.name,
      email: rawData.email,
      password: rawData.password,
      role: this.selectedRole(),
      ...(this.selectedRole() === 'PROVIDER' && {
        bio: rawData.bio,
        experience: rawData.experience
      })
    };

    this.authService.register(payload).subscribe({
      next: () => {
        this.isLoading.set(false);
        this.toast.showToast('Conta criada com sucesso!', 'success');
        this.router.navigate(['/explore']);
      },
      error: (err) => {
        this.isLoading.set(false);
        const errorMsg = err.error?.message || "Erro ao cadastrar. Verifique os dados.";
        this.errorMessage.set(errorMsg);
        this.toast.showToast(errorMsg, 'error');
      }
    });
  }
}
