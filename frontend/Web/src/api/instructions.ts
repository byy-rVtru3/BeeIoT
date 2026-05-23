import { api } from '@/config/api';
import type { ApiResponse } from '@/types/authType';
import type {
  CreateInstructionItemRequest,
  InstructionItem,
  ReorderInstructionItemsRequest,
  UpdateInstructionItemRequest,
} from '@/types/instructionType';

// GET /instruction/items
export const fetchInstructionItems = async (signal?: AbortSignal): Promise<InstructionItem[]> => {
  const response = await api.get<ApiResponse<InstructionItem[]>>('/instruction/items', {
    signal,
  });
  return response.data.data;
};

// GET /admin/instruction/items/
export const fetchAdminInstructionItems = async (
  signal?: AbortSignal
): Promise<InstructionItem[]> => {
  const response = await api.get<ApiResponse<InstructionItem[]>>('/admin/instruction/items/', {
    signal,
  });
  return response.data.data;
};

// POST /admin/instruction/items/
export const createInstructionItem = async (
  data: CreateInstructionItemRequest
): Promise<InstructionItem> => {
  const response = await api.post<ApiResponse<InstructionItem>>('/admin/instruction/items/', data);
  return response.data.data;
};

// PUT /admin/instruction/items/{id}
export const updateInstructionItem = async (
  id: string,
  data: UpdateInstructionItemRequest
): Promise<InstructionItem> => {
  const response = await api.put<ApiResponse<InstructionItem>>(
    `/admin/instruction/items/${id}`,
    data
  );
  return response.data.data;
};

// DELETE /admin/instruction/items/{id}
export const deleteInstructionItem = async (id: string): Promise<void> => {
  await api.delete(`/admin/instruction/items/${id}`);
};

// PUT /admin/instruction/items/reorder
export const reorderInstructionItems = async (
  data: ReorderInstructionItemsRequest
): Promise<InstructionItem[]> => {
  const response = await api.put<ApiResponse<InstructionItem[]>>(
    '/admin/instruction/items/reorder',
    data
  );
  return response.data.data;
};
