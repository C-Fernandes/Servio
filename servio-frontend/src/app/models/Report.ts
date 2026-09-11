export type ReportTargetType = 'SERVICE' | 'USER';

export type ReportReason =
  | 'INAPPROPRIATE_CONTENT'
  | 'SPAM'
  | 'FRAUD'
  | 'HARASSMENT'
  | 'FAKE_PROFILE'
  | 'OTHER';

export type ReportStatus = 'PENDING' | 'REVIEWED' | 'DISMISSED';

export interface ReportResponseDTO {
  id: number;
  targetType: ReportTargetType;
  targetId: number;
  targetLabel: string;
  reporterId: number;
  reporterName: string;
  reason: ReportReason;
  description: string;
  status: ReportStatus;
  createdAt: string;
  reviewedAt: string | null;
}

export interface CreateReportRequestDTO {
  targetType: ReportTargetType;
  targetId: number;
  reason: ReportReason;
  description: string;
}

export interface UpdateReportStatusRequestDTO {
  status: ReportStatus;
}

export const REPORT_REASON_LABELS: Record<ReportReason, string> = {
  INAPPROPRIATE_CONTENT: 'Conteúdo inadequado',
  SPAM: 'Spam ou propaganda indevida',
  FRAUD: 'Fraude ou golpe',
  HARASSMENT: 'Assédio ou comportamento abusivo',
  FAKE_PROFILE: 'Perfil falso',
  OTHER: 'Outro motivo',
};

export const REPORT_STATUS_LABELS: Record<ReportStatus, string> = {
  PENDING: 'Pendente',
  REVIEWED: 'Analisada',
  DISMISSED: 'Arquivada',
};
