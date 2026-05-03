import { Component, inject } from '@angular/core';
import { ToastService } from '../../services/toast/toast.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-toast',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './toast.component.html',
  styleUrl: './toast.component.scss'
})
export class ToastComponent {
  toast = inject(ToastService);

  close() {
    this.toast.show.set(false);
  }
}