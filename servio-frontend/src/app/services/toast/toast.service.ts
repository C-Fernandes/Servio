import { Injectable, signal } from '@angular/core';

export type ToastType = 'success' | 'error' | 'info';

@Injectable({
  providedIn: 'root'
})
export class ToastService {
  message = signal<string>('');
  type = signal<ToastType>('info');
  show = signal<boolean>(false);

  showToast(message: string, type: ToastType = 'success') {
    this.message.set(message);
    this.type.set(type);
    this.show.set(true);

    setTimeout(() => {
      this.show.set(false);
    }, 3000);
  }
}
