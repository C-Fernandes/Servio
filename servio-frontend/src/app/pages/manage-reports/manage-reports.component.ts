import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';

import { ReportService } from '../../services/report/report.service';
import { ToastService } from '../../services/toast/toast.service';
import {
  REPORT_REASON_LABELS,
  REPORT_STATUS_LABELS,
  ReportResponseDTO,
  ReportStatus,
} from '../../models/Report';

type StatusFilter = ReportStatus | 'ALL';

@Component({
  selector: 'app-manage-reports',
  imports: [CommonModule],
  templateUrl: './manage-reports.component.html',
  styleUrl: './manage-reports.component.scss',
})
export class ManageReportsComponent implements OnInit {
  private reportService = inject(ReportService);
  private toast = inject(ToastService);

  reports: ReportResponseDTO[] = [];
  loading = true;
  statusFilter: StatusFilter = 'PENDING';

  readonly reasonLabels = REPORT_REASON_LABELS;
  readonly statusLabels = REPORT_STATUS_LABELS;

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    const status = this.statusFilter === 'ALL' ? undefined : this.statusFilter;

    this.reportService.listAll(status).subscribe({
      next: (data) => {
        this.reports = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Erro ao carregar denúncias:', err);
        this.loading = false;
      },
    });
  }

  setStatusFilter(status: StatusFilter): void {
    this.statusFilter = status;
    this.load();
  }

  markAs(report: ReportResponseDTO, status: ReportStatus): void {
    this.reportService.updateStatus(report.id, status).subscribe({
      next: () => {
        this.toast.showToast('Denúncia atualizada.', 'success');
        this.load();
      },
      error: (err) => {
        console.error('Erro ao atualizar denúncia:', err);
        this.toast.showToast('Não foi possível atualizar a denúncia.', 'error');
      },
    });
  }

  formatDate(date: string): string {
    return new Date(date).toLocaleDateString('pt-BR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  }
}
