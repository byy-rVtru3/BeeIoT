import { useMutation } from '@tanstack/react-query';
import type { UseMutationOptions } from '@tanstack/react-query';

import { signInUser } from '@/api/auth';
import type { LoginRequest, LoginResponse } from '@/types/authType';

type SignInOptions = UseMutationOptions<LoginResponse, unknown, LoginRequest>;

export const useSignInMutation = (options?: SignInOptions) =>
  useMutation({
    mutationFn: (data) => signInUser(data),
    ...options,
  });
