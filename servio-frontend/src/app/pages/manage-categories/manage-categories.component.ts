import { Component, inject, OnInit } from '@angular/core';
import { Category } from '../../models/Category';
import { Tag } from '../../models/Tag';
import { TagService } from '../../services/tag/tag.service';
import { CategoryService } from '../../services/category/category.service';
import { ModalTaxonomyComponent, TaxonomyItem } from '../../components/modal-taxonomy/modal-taxonomy.component';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ToastService } from '../../services/toast/toast.service';

@Component({
  selector: 'app-manage-categories',
  imports: [CommonModule, ModalTaxonomyComponent, FormsModule],
  templateUrl: './manage-categories.component.html',
  styleUrl: './manage-categories.component.scss',
}) export class ManageCategoriesComponent implements OnInit {
  viewMode: 'categories' | 'tags' = 'categories';

  private toast = inject(ToastService);
  categories: Category[] = [];
  tags: Tag[] = [];

  isModalOpen = false;
  modalTitle = '';
  selectedItem: TaxonomyItem = { name: '' };

  private categoryService = inject(CategoryService);
  private tagService = inject(TagService);

  ngOnInit() {
    this.loadData();
  }

  loadData() {
    this.categoryService.findAll().subscribe({
      next: (data) => (this.categories = data),
      error: (err) => console.error('Erro ao carregar categorias', err),
    });

    this.tagService.findAll().subscribe({
      next: (data) => (this.tags = data),
      error: (err) => console.error('Erro ao carregar tags', err),
    });
  }

  openAdd() {
    this.modalTitle = this.viewMode === 'categories' ? 'Criar Categoria' : 'Criar Tag';
    this.selectedItem = { name: '' };
    this.isModalOpen = true;
  }

  openEdit(item: any) {
    this.modalTitle = this.viewMode === 'categories' ? 'Editar Categoria' : 'Editar Tag';
    this.selectedItem = { id: item.id, name: item.name };
    this.isModalOpen = true;
  }

  deleteItem(id: number) {
    const confirmDelete = confirm('Tem certeza que deseja excluir este item?');
    if (!confirmDelete) return;

    if (this.viewMode === 'categories') {
      this.categoryService.delete(id).subscribe({
        next: () => {
          this.toast.showToast('Categoria removida com sucesso!', 'success');
          this.loadData();
        },
        error: (err) => {
          this.toast.showToast(this.getErrorMessage(err), 'error');
        },
      });
    } else {
      this.tagService.delete(id).subscribe({
        next: () => {
          this.toast.showToast('Tag removida com sucesso!', 'success');
          this.loadData();
        },
        error: (err) => {
          this.toast.showToast(this.getErrorMessage(err), 'error');
        },
      });
    }
  }

  handleSave(updatedItem: TaxonomyItem) {
    const service = this.viewMode === 'categories' ? this.categoryService : this.tagService;

    if (updatedItem.id) {
      service.update(updatedItem.id, updatedItem).subscribe(() => {
        this.loadData();
        this.isModalOpen = false;
      });
    } else {
      service.create(updatedItem).subscribe(() => {
        this.loadData();
        this.isModalOpen = false;
      });
    }
  } private getErrorMessage(err: any): string {
    return err?.error?.message || 'Ocorreu um erro inesperado.';
  }
}