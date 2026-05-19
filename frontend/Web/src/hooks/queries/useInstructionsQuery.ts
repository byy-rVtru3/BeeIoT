import { queryOptions, useQuery } from '@tanstack/react-query';

import { fetchInstructions } from '@/api/instructions';

export const instructionsQueryOptions = () =>
  queryOptions({
    queryKey: ['instructions'],
    queryFn: ({ signal }) => fetchInstructions(signal),
  });

export const useInstructionsQuery = () => useQuery(instructionsQueryOptions());
