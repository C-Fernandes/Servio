import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { CouponService } from '../../services/coupon/coupon.service';
import { ServiceService } from '../../services/service/service.service';
import { ToastService } from '../../services/toast/toast.service';
import { CouponResponseDTO, CouponCreateRequestDTO } from '../../models/Coupon';
import { Service } from '../../models/Service';

@Component({
  selector: 'app-coupons',
  imports: [CommonModule, FormsModule],
  templateUrl: './coupons.component.html',
  styleUrl: './coupons.component.scss',
})
export class CouponsComponent {
  private couponService = inject(CouponService);
  private serviceService = inject(ServiceService);
  private toast = inject(ToastService);

  coupons: CouponResponseDTO[] = [];
  myServices: Service[] = [];

  isFormOpen = false;
  isSaving = false;

  form: { serviceId: number | null; code: string; discountPercentage: number | null; expiresAt: string } = {
    serviceId: null,
    code: '',
    discountPercentage: null,
    expiresAt: '',
  };

  ngOnInit(): void {
    this.loadCoupons();
    this.serviceService.findMyServices().subscribe({
      next: (services) => (this.myServices = services),
      error: (err) => console.error('Erro ao carregar serviços:', err),
    });
  }

  loadCoupons() {
    this.couponService.findMine().subscribe({
      next: (data) => (this.coupons = data),
      error: (err) => {
        console.error('Erro ao carregar cupons:', err);
        this.toast.showToast('Não foi possível carregar seus cupons.', 'error');
      },
    });
  }

  openForm() {
    this.isFormOpen = true;
  }

  closeForm() {
    this.isFormOpen = false;
    this.form = { serviceId: null, code: '', discountPercentage: null, expiresAt: '' };
  }

  submitForm() {
    if (!this.form.serviceId || !this.form.code.trim() || !this.form.discountPercentage) {
      this.toast.showToast('Preencha serviço, código e percentual de desconto.', 'error');
      return;
    }

    const dto: CouponCreateRequestDTO = {
      serviceId: this.form.serviceId,
      code: this.form.code.trim().toUpperCase(),
      discountPercentage: this.form.discountPercentage,
      expiresAt: this.form.expiresAt ? new Date(this.form.expiresAt).toISOString() : null,
    };

    this.isSaving = true;
    this.couponService.create(dto).subscribe({
      next: (coupon) => {
        this.isSaving = false;
        this.coupons.unshift(coupon);
        this.toast.showToast('Cupom criado com sucesso!', 'success');
        this.closeForm();
      },
      error: (err) => {
        this.isSaving = false;
        this.toast.showToast(err.error?.message ?? 'Não foi possível criar o cupom.', 'error');
      },
    });
  }

  deactivate(coupon: CouponResponseDTO) {
    if (!confirm(`Desativar o cupom "${coupon.code}"?`)) {
      return;
    }

    this.couponService.deactivate(coupon.id).subscribe({
      next: () => {
        coupon.active = false;
        this.toast.showToast('Cupom desativado.', 'success');
      },
      error: (err) => {
        console.error('Erro ao desativar cupom:', err);
        this.toast.showToast('Não foi possível desativar o cupom.', 'error');
      },
    });
  }
}
