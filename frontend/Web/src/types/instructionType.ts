// Серверный контракт — backend/internal/domain/models/httpType.InstructionItem.
// id у бэка — UUID (строка), а не число, и поле текста называется `body`.
export type InstructionItem = {
  id: string;
  title: string;
  body: string;
  numbered: boolean;
  position: number;
};

// CreateInstructionItemRequest на сервере. numbered/position опциональны.
export type CreateInstructionRequest = {
  title: string;
  body: string;
  numbered?: boolean;
  position?: number;
};

// Сервер возвращает свежесозданный пункт целиком (см. itemToHTTP),
// а не объект { id } — так что переиспользуем InstructionItem.
export type CreateInstructionResponse = InstructionItem;
