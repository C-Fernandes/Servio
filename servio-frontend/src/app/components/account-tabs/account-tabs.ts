import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';

type AccountTab = 'personal' | 'contact' | 'address';

@Component({
  selector: 'app-account-tabs',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './account-tabs.html',
  styleUrl: './account-tabs.scss',
})
export class AccountTabsComponent {
  @Input() activeTab: AccountTab = 'personal';
  @Output() tabChange = new EventEmitter<AccountTab>();

  selectTab(tab: AccountTab): void {
    this.tabChange.emit(tab);
  }
}
