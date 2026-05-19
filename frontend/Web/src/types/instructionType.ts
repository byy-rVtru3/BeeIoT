export type InstructionItem = {
  id: number;
  title: string;
  content: string;
  created_at: number;
};

export type CreateInstructionRequest = {
  title: string;
  content: string;
};

export type CreateInstructionResponse = {
  id: number;
};
