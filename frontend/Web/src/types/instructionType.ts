export type InstructionItem = {
  id: string;
  title: string;
  body: string;
  numbered: boolean;
  position: number;
  updated_at: string;
};

export type CreateInstructionItemRequest = {
  title: string;
  body: string;
  numbered: boolean;
  position?: number;
};

export type UpdateInstructionItemRequest = {
  title?: string;
  body?: string;
  numbered?: boolean;
};

export type ReorderInstructionItemsRequest = {
  order: string[];
};
