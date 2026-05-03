import { CommonModule } from '@angular/common';
import { Component, EventEmitter, inject, Output } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { TagService } from '../../services/tag/tag.service';
import { CategoryService } from '../../services/category/category.service';

@Component({
  selector: 'app-service-modal',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './service-modal.component.html',
  styleUrl: './service-modal.component.scss',
})
export class ServiceModalComponent {
  @Output() closeEvent = new EventEmitter<void>();
  @Output() saveEvent = new EventEmitter<any>();

  private categoryService = inject(CategoryService);
  private tagService = inject(TagService);

  serviceForm: FormGroup;

  isDragging = false;
  previewUrl: string | ArrayBuffer | null = null;
  selectedFile: File | null = null;

  categories: any[] = [];
  allTags: any[] = [];

  selectedTags: any[] = [];
  filteredSuggestions: any[] = [];

  constructor(private fb: FormBuilder) {
    this.serviceForm = this.fb.group({
      title: ['', Validators.required],
      description: ['', Validators.required],
      price: ['', [Validators.required, Validators.min(0)]],
      durationInMinutes: ['', [Validators.required, Validators.min(1)]],
      category: ['', Validators.required], tagInput: ['']
    });
  }

  ngOnInit(): void {
    this.categoryService.findAll().subscribe(res => this.categories = res);

    this.tagService.findAll().subscribe(res => this.allTags = res);
  }

  getSelectedCategoryName(): string {
    const categoryId = this.serviceForm.get('category')?.value;
    const category = this.categories.find(c => c.id === Number(categoryId));
    return category ? category.name : 'Choose a category';
  }
  selectCategory(category: any): void {
    this.serviceForm.patchValue({ category: category.id });
  }
  onTagFocus(): void {
    this.filterTags(this.serviceForm.get('tagInput')?.value || '');
  }

  onTagTyping(event: any): void {
    const value = event.target.value?.toLowerCase() || '';
    this.filterTags(value);
  }

  private filterTags(value: string): void {
    if (this.allTags) {
      this.filteredSuggestions = this.allTags.filter(tag =>
        tag?.name &&
        tag.name.toLowerCase().includes(value) &&
        !this.selectedTags.some(t => t.id === tag.id)
      );
    }
  }
  hideSuggestionsWithDelay(): void {
    setTimeout(() => {
      this.filteredSuggestions = [];
    }, 200);
  }
  addTag(tag: any): void {
    if (this.selectedTags.length < 6) {
      this.selectedTags.push(tag);
      this.serviceForm.get('tagInput')?.setValue('');
      this.filteredSuggestions = [];
    }
  }

  removeTag(tagId: number): void {
    this.selectedTags = this.selectedTags.filter(t => t.id !== tagId);
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
        title: this.serviceForm.value.title,
        description: this.serviceForm.value.description,
        price: this.serviceForm.value.price,
        durationInMinutes: this.serviceForm.value.durationInMinutes,
        category: this.serviceForm.value.category,
        tags: this.selectedTags.map(tag => tag.id),
        imageFile: this.selectedFile,
      };

      console.log('Dados emitidos pelo Modal:', formData);

      this.saveEvent.emit(formData);
    } else {
      this.serviceForm.markAllAsTouched();
    }
  }
}