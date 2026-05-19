import { useMutation, useQueryClient } from '@tanstack/react-query';
import type { UseMutationOptions } from '@tanstack/react-query';

import { deleteInstruction } from '@/api/instructions';

type DeleteInstructionOptions = UseMutationOptions<void, unknown, number>;

export const useDeleteInstructionMutation = (options?: DeleteInstructionOptions) => {
  const queryClient = useQueryClient();
  const { onSuccess, ...restOptions } = options ?? {};

  return useMutation({
    mutationFn: (id) => deleteInstruction(id),
    ...restOptions,
    onSuccess: async (data, variables, onMutateResult, context) => {
      await queryClient.invalidateQueries({ queryKey: ['instructions'] });
      await onSuccess?.(data, variables, onMutateResult, context);
    },
  });
};
