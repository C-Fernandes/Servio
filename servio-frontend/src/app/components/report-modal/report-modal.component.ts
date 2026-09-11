import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';

import {
  CreateReportRequestDTO,
  REPORT_REASON_LABELS,
  ReportReason,
  ReportTargetType,
} from '../../models/Report';

export interface ReportTarget {
  targetType: ReportTargetType;
  targetId: number;
  targetLabel: string;
}

@Component({
  selector: 'app-report-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './report-modal.component.html',
  styleUrl: './report-modal.component.scss',
})
export class ReportModalComponent {
  @Input({ required: true }) target!: ReportTarget | null;

  @Output() close = new EventEmitter<void>();
  @Output() submitReport = new EventEmitter<CreateReportRequestDTO>();

  reason: ReportReason | '' = '';
  description = '';

  readonly reasons: { value: ReportReason; label: string }[] = Object.entries(REPORT_REASON_LABELS).map(
    ([value, label]) => ({ value: value as ReportReason, label })
  );

  get canSubmit(): boolean {
    return this.reason !== '' && this.description.trim().length > 0;
  }

  onClose() {
    this.reset();
    this.close.emit();
  }

  onSubmit() {
    if (!this.target || !this.canSubmit) {
      return;
    }

    this.submitReport.emit({
      targetType: this.target.targetType,
      targetId: this.target.targetId,
      reason: this.reason as ReportReason,
      description: this.description.trim(),
    });

    this.reset();
  }

  private reset() {
    this.reason = '';
    this.description = '';
  }
}
