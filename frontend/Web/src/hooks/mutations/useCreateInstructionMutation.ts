import { useMutation, useQueryClient } from '@tanstack/react-query';
import type { UseMutationOptions } from '@tanstack/react-query';

import { createInstruction } from '@/api/instructions';
import type { CreateInstructionRequest, CreateInstructionResponse } from '@/types/instructionType';

type CreateInstructionOptions = UseMutationOptions<
  CreateInstructionResponse,
  unknown,
  CreateInstructionRequest
>;

export const useCreateInstructionMutation = (options?: CreateInstructionOptions) => {
  const queryClient = useQueryClient();
  const { onSuccess, ...restOptions } = options ?? {};

  return useMutation({
    mutationFn: (data) => createInstruction(data),
    ...restOptions,
    onSuccess: async (data, variables, onMutateResult, context) => {
      await queryClient.invalidateQueries({ queryKey: ['instructions'] });
      await onSuccess?.(data, variables, onMutateResult, context);
    },
  });
};
