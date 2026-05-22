import { queryOptions, useQuery } from '@tanstack/react-query';

import { fetchAdminInstructionItems } from '@/api/instructions';

export const instructionsQueryOptions = () =>
  queryOptions({
    queryKey: ['instruction-items', 'admin'],
    queryFn: ({ signal }) => fetchAdminInstructionItems(signal),
  });

export const useInstructionsQuery = () => useQuery(instructionsQueryOptions());
