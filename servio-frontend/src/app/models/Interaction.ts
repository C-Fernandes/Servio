export type InteractionEventType = 'ORDER_STATUS' | 'REVIEW' | 'MESSAGE';

export interface InteractionEventDTO {
  type: InteractionEventType;
  timestamp: string;
  description: string;
  referenceId: number;
}

export const INTERACTION_ICONS: Record<InteractionEventType, string> = {
  ORDER_STATUS: 'shopping_bag',
  REVIEW: 'star',
  MESSAGE: 'chat',
};
