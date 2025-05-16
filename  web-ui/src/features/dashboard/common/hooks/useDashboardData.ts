import { useQuery, UseQueryOptions, QueryKey } from '@tanstack/react-query';

// Define a generic type for the fetch function
type FetchFunction<TData, TParams = unknown> = (params?: TParams) => Promise<TData>;

// Define the props for the hook
interface UseDashboardDataOptions<TData, TError, TParams = unknown>
  extends Omit<UseQueryOptions<TData, TError, TData, QueryKey>, 'queryKey' | 'queryFn'> {
  params?: TParams; // Optional parameters for the fetch function
}

const useDashboardData = <TData, TError = Error, TParams = unknown>(
  queryKey: QueryKey,
  fetchFn: FetchFunction<TData, TParams>,
  options?: UseDashboardDataOptions<TData, TError, TParams>
) => {
  const { params, ...queryOptions } = options || {};

  return useQuery<TData, TError, TData, QueryKey>({
    queryKey: params ? [...queryKey, params] : queryKey, // Include params in queryKey for unique caching
    queryFn: () => fetchFn(params),
    staleTime: 5 * 60 * 1000, // Data considered fresh for 5 minutes by default
    refetchOnWindowFocus: false, // Optional: disable refetch on window focus
    ...queryOptions, // Spread other react-query options
  });
};

export default useDashboardData;