import { queryOptions, useQuery } from '@tanstack/react-query';

import { fetchSession } from '@/api/auth';

export const sessionQueryOptions = () =>
  queryOptions({
    queryKey: ['session'],
    queryFn: ({ signal }) => fetchSession(signal),
  });

export const useSessionQuery = () => useQuery(sessionQueryOptions());
