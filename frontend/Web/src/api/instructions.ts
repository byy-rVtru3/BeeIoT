import { api } from '@/config/api';
import type { ApiResponse } from '@/types/authType';
import type {
  CreateInstructionRequest,
  CreateInstructionResponse,
  InstructionItem,
} from '@/types/instructionType';

// GET /instructions/list
export const fetchInstructions = async (signal?: AbortSignal): Promise<InstructionItem[]> => {
  const response = await api.get<ApiResponse<InstructionItem[]>>('/instructions/list', { signal });
  return response.data.data;
};

// POST /instructions/create
export const createInstruction = async (
  data: CreateInstructionRequest
): Promise<CreateInstructionResponse> => {
  const response = await api.post<CreateInstructionResponse>('/instructions/create', data);
  return response.data;
};

// DELETE /instructions/{id}
export const deleteInstruction = async (id: number): Promise<void> => {
  await api.delete(`/instructions/${id}`);
};
