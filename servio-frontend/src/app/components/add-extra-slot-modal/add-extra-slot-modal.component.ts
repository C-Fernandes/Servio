import { Component, EventEmitter, inject, Output } from '@angular/core';
import { ToastService } from '../../services/toast/toast.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-add-extra-slot-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './add-extra-slot-modal.component.html',
  styleUrl: './add-extra-slot-modal.component.scss'
})
export class AddExtraSlotModalComponent {
  private toast = inject(ToastService);

  @Output() closeModal = new EventEmitter<void>();
  @Output() confirmSlot = new EventEmitter<{ startDate: string, startTime: string, endTime: string }>();

  newSlotStart: string = '';
  newSlotEnd: string = '';

  onClose(): void {
    this.closeModal.emit();
  }

  onConfirm(): void {
    if (!this.newSlotStart || !this.newSlotEnd) {
      this.toast.showToast("Preencha o início e o fim", "error");
      return;
    }

    const startDate = this.newSlotStart.split('T')[0];
    const startTime = this.newSlotStart.split('T')[1];
    const endTime = this.newSlotEnd.split('T')[1];

    this.confirmSlot.emit({ startDate, startTime, endTime });
  }
}