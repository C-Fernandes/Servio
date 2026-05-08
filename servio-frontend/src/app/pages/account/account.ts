import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { AccountHeaderCardComponent } from '../../components/account-header-card/account-header-card';
import { AccountTabsComponent } from '../../components/account-tabs/account-tabs';
import { AccountPersonalFormComponent } from '../../components/account-personal-form/account-personal-form';
import { AccountContactFormComponent } from '../../components/account-contact-form/account-contact-form';
import { AccountAddressFormComponent } from '../../components/account-address-form/account-address-form';
import { AuthService } from '../../services/auth/auth.service';
import { ToastService } from '../../services/toast/toast.service';
import { UserService, UserUpdateRequestDTO } from '../../services/user/user';

type AccountTab = 'personal' | 'contact' | 'address';

@Component({
  selector: 'app-account',
  standalone: true,
  imports: [
    CommonModule,
    AccountHeaderCardComponent,
    AccountTabsComponent,
    AccountPersonalFormComponent,
    AccountContactFormComponent,
    AccountAddressFormComponent,
  ],
  templateUrl: './account.html',
  styleUrl: './account.scss',
})
export class AccountComponent implements OnInit {
  private authService = inject(AuthService);
  private userService = inject(UserService);
  private toast = inject(ToastService);

  activeTab: AccountTab = 'personal';
  userId: number | null = null;

  personal = {
    fullName: '',
  };

  contact = {
    email: '',
    phone: '',
  };

  address = {
    zipCode: '',
    street: '',
    number: '',
    complement: '',
    district: '',
    city: '',
    state: '',
  };

  ngOnInit(): void {
    this.loadUser();
  }

  setTab(tab: AccountTab): void {
    this.activeTab = tab;
  }

  loadUser(): void {
    const userId = this.authService.getUserId();

    if (!userId) {
      this.toast.showToast('Não foi possível identificar o usuário.', 'error');
      return;
    }

    this.userId = userId;

    this.userService.findById(userId).subscribe({
      next: (user) => {
        this.personal.fullName = user.name ?? '';
        this.contact.email = user.email ?? '';
        this.contact.phone = user.phone ?? '';

        this.address = {
          zipCode: user.zipCode ?? '',
          street: user.street ?? '',
          number: user.number ?? '',
          complement: user.complement ?? '',
          district: user.neighborhood ?? '',
          city: user.city ?? '',
          state: user.state ?? '',
        };
      },
      error: () => {
        this.toast.showToast('Erro ao carregar dados da conta.', 'error');
      },
    });
  }

  private isPhoneValid(phone: string): boolean {
    const digits = (phone ?? '').replace(/\D/g, '');
    return digits.length === 10 || digits.length === 11;
  }

  private isNameValid(name: string): boolean {
    const normalizedName = (name ?? '').trim();
    return normalizedName.length >= 3;
  }

  private hasAnyAddressData(): boolean {
    return !!(
      this.address.zipCode?.trim() ||
      this.address.street?.trim() ||
      this.address.number?.trim() ||
      this.address.complement?.trim() ||
      this.address.district?.trim() ||
      this.address.city?.trim() ||
      this.address.state?.trim()
    );
  }

  private isAddressValid(): boolean {
    if (!this.hasAnyAddressData()) {
      return true;
    }

    return !!(
      this.address.zipCode?.trim() &&
      this.address.city?.trim() &&
      this.address.state?.trim()
    );
  }

  save(): void {
    if (!this.userId) {
      this.toast.showToast('Usuário não identificado.', 'error');
      return;
    }

    if (!this.isNameValid(this.personal.fullName)) {
      this.activeTab = 'personal';
      this.toast.showToast('O nome deve ter pelo menos 3 letras.', 'error');
      return;
    }

    if (this.contact.phone && !this.isPhoneValid(this.contact.phone)) {
      this.activeTab = 'contact';
      this.toast.showToast('Telefone inválido. Preencha um número válido antes de salvar.', 'error');
      return;
    }

    if (!this.isAddressValid()) {
      this.activeTab = 'address';
      this.toast.showToast('Ao preencher endereço, CEP, cidade e UF são obrigatórios.', 'error');
      return;
    }

    const payload: UserUpdateRequestDTO = {
      name: this.personal.fullName.trim(),
      phone: this.contact.phone || null,
      street: this.address.street || null,
      number: this.address.number || null,
      complement: this.address.complement || null,
      neighborhood: this.address.district || null,
      zipCode: this.address.zipCode || null,
      city: this.address.city || null,
      state: this.address.state || null,
    };

    this.userService.update(this.userId, payload).subscribe({
      next: (updatedUser) => {
        this.personal.fullName = updatedUser.name ?? this.personal.fullName;
        this.contact.email = updatedUser.email ?? this.contact.email;
        this.contact.phone = updatedUser.phone ?? this.contact.phone;

        this.address = {
          zipCode: updatedUser.zipCode ?? this.address.zipCode,
          street: updatedUser.street ?? this.address.street,
          number: updatedUser.number ?? this.address.number,
          complement: updatedUser.complement ?? this.address.complement,
          district: updatedUser.neighborhood ?? this.address.district,
          city: updatedUser.city ?? this.address.city,
          state: updatedUser.state ?? this.address.state,
        };

        this.toast.showToast('Dados atualizados com sucesso.', 'success');
      },
      error: () => {
        this.toast.showToast('Erro ao salvar alterações.', 'error');
      },
    });
  }
}
