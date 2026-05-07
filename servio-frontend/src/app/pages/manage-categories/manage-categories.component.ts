import { Component, inject, OnInit } from '@angular/core';
import { Category } from '../../models/Category';
import { Tag } from '../../models/Tag';
import { TagService } from '../../services/tag/tag.service';
import { CategoryService } from '../../services/category/category.service';
import { ModalTaxonomyComponent, TaxonomyItem } from '../../components/modal-taxonomy/modal-taxonomy.component';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-manage-categories',
  imports: [CommonModule, ModalTaxonomyComponent, FormsModule],
  templateUrl: './manage-categories.component.html',
  styleUrl: './manage-categories.component.scss',
}) export class ManageCategoriesComponent implements OnInit {
  viewMode: 'categories' | 'tags' = 'categories';

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
          this.loadData(); // Recarrega os dados após deletar
        },
        error: (err) => console.error('Erro ao excluir categoria', err),
      });
    } else {
      this.tagService.delete(id).subscribe({
        next: () => {
          this.loadData(); // Recarrega os dados após deletar
        },
        error: (err) => console.error('Erro ao excluir tag', err),
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
  }
}