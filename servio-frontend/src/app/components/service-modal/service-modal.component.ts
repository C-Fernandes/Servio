import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Output } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';

@Component({
  selector: 'app-service-modal',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './service-modal.component.html',
  styleUrl: './service-modal.component.scss',
})
export class ServiceModalComponent {
  @Output() closeEvent = new EventEmitter<void>();
  @Output() saveEvent = new EventEmitter<any>();

  serviceForm: FormGroup;

  isDragging = false;
  previewUrl: string | ArrayBuffer | null = null;
  selectedFile: File | null = null;

  constructor(private fb: FormBuilder) {
    this.serviceForm = this.fb.group({
      title: ['', Validators.required],
      description: ['', Validators.required],
      price: ['', [Validators.required, Validators.min(0)]],
      duration: [''],
      category: ['', Validators.required]
    });
  }

  close() {
    this.closeEvent.emit();
  }

  onDragOver(event: DragEvent) {
    event.preventDefault();
    this.isDragging = true;
  }

  onDragLeave(event: DragEvent) {
    event.preventDefault();
    this.isDragging = false;
  }

  onDrop(event: DragEvent) {
    event.preventDefault();
    this.isDragging = false;
    if (event.dataTransfer?.files && event.dataTransfer.files.length > 0) {
      this.handleFile(event.dataTransfer.files[0]);
    }
  }

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.handleFile(input.files[0]);
    }
  }

  handleFile(file: File) {
    if (file.type.match(/image\/*/) == null) {
      alert('Por favor, selecione apenas imagens.');
      return;
    }

    this.selectedFile = file;

    const reader = new FileReader();
    reader.onload = () => {
      this.previewUrl = reader.result;
    };
    reader.readAsDataURL(file);
  }

  removeImage(event: Event) {
    event.stopPropagation();
    this.previewUrl = null;
    this.selectedFile = null;
  }

  onSubmit() {
    if (this.serviceForm.valid) {
      const formData = {
        ...this.serviceForm.value,
        imageFile: this.selectedFile
      };
      this.saveEvent.emit(formData);
    } else {
      this.serviceForm.markAllAsTouched();
    }
  }
}