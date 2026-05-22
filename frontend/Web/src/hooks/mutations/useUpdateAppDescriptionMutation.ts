import { useMutation, useQueryClient } from '@tanstack/react-query';
import type { UseMutationOptions } from '@tanstack/react-query';

import { updateAdminAppDescription } from '@/api/appDescription';
import type { AppDescription, UpdateAppDescriptionRequest } from '@/types/appDescriptionType';

type UpdateAppDescriptionOptions = UseMutationOptions<
  AppDescription,
  unknown,
  UpdateAppDescriptionRequest
>;

export const useUpdateAppDescriptionMutation = (options?: UpdateAppDescriptionOptions) => {
  const queryClient = useQueryClient();
  const { onSuccess, ...restOptions } = options ?? {};

  return useMutation({
    mutationFn: (data) => updateAdminAppDescription(data),
    ...restOptions,
    onSuccess: async (data, variables, onMutateResult, context) => {
      await queryClient.invalidateQueries({ queryKey: ['app-description', 'admin'] });
      await onSuccess?.(data, variables, onMutateResult, context);
    },
  });
};
