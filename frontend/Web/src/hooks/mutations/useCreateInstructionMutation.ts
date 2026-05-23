import { useMutation, useQueryClient } from '@tanstack/react-query';
import type { UseMutationOptions } from '@tanstack/react-query';

import { createInstructionItem } from '@/api/instructions';
import type { CreateInstructionItemRequest, InstructionItem } from '@/types/instructionType';

type CreateInstructionOptions = UseMutationOptions<
  InstructionItem,
  unknown,
  CreateInstructionItemRequest
>;

export const useCreateInstructionMutation = (options?: CreateInstructionOptions) => {
  const queryClient = useQueryClient();
  const { onSuccess, ...restOptions } = options ?? {};

  return useMutation({
    mutationFn: (data) => createInstructionItem(data),
    ...restOptions,
    onSuccess: async (data, variables, onMutateResult, context) => {
      await queryClient.invalidateQueries({ queryKey: ['instruction-items', 'admin'] });
      await onSuccess?.(data, variables, onMutateResult, context);
    },
  });
};
