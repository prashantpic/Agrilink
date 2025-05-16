import { useEffect, useState } from 'react';
import { Model, Query } from '@nozbe/watermelondb';
import { Observable, Subscription } from 'rxjs';

// REQ-14-009: Hook to observe WatermelonDB queries and provide reactive local data to UI components.

/**
 * Custom React hook to observe WatermelonDB queries or observables and provide reactive data.
 *
 * @param queryOrObservable - A WatermelonDB Query instance, or a direct RxJS Observable
 *                            returning a single Model instance or an array of Model instances.
 * @returns The observed data (Model instance or array of Model instances), or undefined while loading.
 */

// Overload signatures for better type inference based on input
function useOfflineData<T extends Model>(query: Query<T>): T[] | undefined;
function useOfflineData<T extends Model>(observable: Observable<T>): T | undefined;
function useOfflineData<T extends Model>(observable: Observable<T[]>): T[] | undefined;

function useOfflineData<T extends Model>(
  queryOrObservable: Query<T> | Observable<T> | Observable<T[]>
): T[] | T | undefined {
  const [data, setData] = useState<T[] | T | undefined>(undefined);

  useEffect(() => {
    let currentObservable: Observable<T[] | T>;

    if (queryOrObservable instanceof Query) {
      // If a Query object is passed, observe it. It typically emits Model[].
      currentObservable = queryOrObservable.observe() as Observable<T[]>;
    } else {
      // If an Observable is passed directly.
      currentObservable = queryOrObservable as Observable<T[] | T>;
    }

    const subscription: Subscription = currentObservable.subscribe(
      (result: T[] | T) => {
        setData(result);
      }
    );

    // Cleanup subscription on component unmount or if queryOrObservable changes
    return () => {
      subscription.unsubscribe();
    };
  }, [queryOrObservable]); // Re-run effect if the query or observable instance changes

  return data;
}

export default useOfflineData;