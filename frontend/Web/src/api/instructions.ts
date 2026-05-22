import { api } from '@/config/api';
import type { ApiResponse } from '@/types/authType';
import type {
  CreateInstructionRequest,
  InstructionItem,
} from '@/types/instructionType';

// Серверные роуты для админ-панели — backend/internal/http/router.go:120-126.
// Чтение списка инструкций есть и публично (`/instruction/items`), но из-под
// админки имеет смысл идти через admin-префикс — он защищён CheckAdmin и
// при потере прав мы сразу увидим 401/403, а не молча тянем чужой контент.
const INSTRUCTION_ITEMS_BASE = '/admin/instruction/items';

// GET /api/admin/instruction/items/
export const fetchInstructions = async (signal?: AbortSignal): Promise<InstructionItem[]> => {
  const response = await api.get<ApiResponse<InstructionItem[]>>(`${INSTRUCTION_ITEMS_BASE}/`, {
    signal,
  });
  return response.data.data;
};

// POST /api/admin/instruction/items/
export const createInstruction = async (
  data: CreateInstructionRequest
): Promise<InstructionItem> => {
  const response = await api.post<ApiResponse<InstructionItem>>(`${INSTRUCTION_ITEMS_BASE}/`, data);
  return response.data.data;
};

// DELETE /api/admin/instruction/items/{id}
export const deleteInstruction = async (id: string): Promise<void> => {
  await api.delete(`${INSTRUCTION_ITEMS_BASE}/${id}`);
};
