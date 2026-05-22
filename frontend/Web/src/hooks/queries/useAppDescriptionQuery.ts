import { queryOptions, useQuery } from '@tanstack/react-query';

import { fetchAdminAppDescription } from '@/api/appDescription';

export const appDescriptionQueryOptions = () =>
  queryOptions({
    queryKey: ['app-description', 'admin'],
    queryFn: ({ signal }) => fetchAdminAppDescription(signal),
  });

export const useAppDescriptionQuery = () => useQuery(appDescriptionQueryOptions());
