import { useMutation } from '@tanstack/react-query';
import type { UseMutationOptions } from '@tanstack/react-query';

import { logoutUser } from '@/api/auth';
import type { ApiResponse } from '@/types/authType';

type LogoutOptions = UseMutationOptions<ApiResponse<null>, unknown, void>;

export const useLogoutMutation = (options?: LogoutOptions) =>
  useMutation({
    mutationFn: () => logoutUser(),
    ...options,
  });
