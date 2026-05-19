import { useEffect, useCallback } from 'react';
import { useObserveStore } from '@/stores/observeStore';
import { getMetricsSnapshot, getRecentTraces } from '@/api/observe';

const REFRESH_INTERVAL = 30000; // 30 seconds

export function useMetrics() {
  const { metricsSnapshot, recentTraces, setMetrics, setTraces, setLoading, setError } =
    useObserveStore();

  const refresh = useCallback(async () => {
    setLoading(true);
    try {
      const [metricsRes, tracesRes] = await Promise.allSettled([
        getMetricsSnapshot(),
        getRecentTraces(20),
      ]);

      if (metricsRes.status === 'fulfilled') {
        setMetrics(metricsRes.value.data as any);
      }
      if (tracesRes.status === 'fulfilled') {
        setTraces(tracesRes.value.data as any);
      }
      setError(null);
    } catch (err: any) {
      setError(err.message || '获取指标数据失败');
    } finally {
      setLoading(false);
    }
  }, [setMetrics, setTraces, setLoading, setError]);

  useEffect(() => {
    refresh();
    const timer = setInterval(refresh, REFRESH_INTERVAL);
    return () => clearInterval(timer);
  }, [refresh]);

  return { metricsSnapshot, recentTraces, refresh };
}
