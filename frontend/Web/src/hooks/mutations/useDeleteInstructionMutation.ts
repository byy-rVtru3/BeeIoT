import { useMutation, useQueryClient } from '@tanstack/react-query';
import type { UseMutationOptions } from '@tanstack/react-query';

import { deleteInstructionItem } from '@/api/instructions';

type DeleteInstructionOptions = UseMutationOptions<void, unknown, string>;

export const useDeleteInstructionMutation = (options?: DeleteInstructionOptions) => {
  const queryClient = useQueryClient();
  const { onSuccess, ...restOptions } = options ?? {};

  return useMutation({
    mutationFn: (id) => deleteInstructionItem(id),
    ...restOptions,
    onSuccess: async (data, variables, onMutateResult, context) => {
      await queryClient.invalidateQueries({ queryKey: ['instruction-items', 'admin'] });
      await onSuccess?.(data, variables, onMutateResult, context);
    },
  });
};
