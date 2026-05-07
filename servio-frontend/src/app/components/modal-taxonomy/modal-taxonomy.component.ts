import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
export interface TaxonomyItem {
  id?: number | null;
  name: string;
}
@Component({
  selector: 'app-modal-taxonomy',
  imports: [FormsModule],
  templateUrl: './modal-taxonomy.component.html',
  styleUrl: './modal-taxonomy.component.scss',
})
export class ModalTaxonomyComponent {
  @Input() title: string = 'Criar';
  @Input() set data(value: TaxonomyItem) {
    this.formData = { ...value };
  }
  @Output() close = new EventEmitter<void>();
  @Output() save = new EventEmitter<TaxonomyItem>();

  formData: TaxonomyItem = { name: '' };

  onClose() {
    this.close.emit();
  }

  onSave() {
    if (this.formData.name && this.formData.name.trim() !== '') {
      this.save.emit(this.formData);
    }
  }
}
