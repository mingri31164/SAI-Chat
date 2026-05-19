import { useCallback, useRef, useState } from 'react';
import type { SSEEventName } from '@/utils/sse-parser';

export type SSEStatus = 'idle' | 'connecting' | 'connected' | 'error' | 'closed';

export interface SSEOptions {
  autoReconnect?: boolean;
  maxRetries?: number;
  retryDelay?: number;
  onStatusChange?: (status: SSEStatus) => void;
}

export interface SSEState {
  status: SSEStatus;
  error: string | null;
  lastEvent: { name: SSEEventName; data: unknown } | null;
}

export function useSSE(options: SSEOptions = {}) {
  const {
    autoReconnect = true,
    maxRetries = 5,
    retryDelay = 3000,
    onStatusChange,
  } = options;

  const [state, setState] = useState<SSEState>({
    status: 'idle',
    error: null,
    lastEvent: null,
  });

  const esRef = useRef<EventSource | null>(null);
  const retryCountRef = useRef(0);
  const retryTimerRef = useRef<ReturnType<typeof setTimeout>>();
  const handlersRef = useRef<Map<SSEEventName, (data: unknown) => void>>(new Map());

  const setStatus = useCallback(
    (status: SSEStatus) => {
      setState((prev) => ({ ...prev, status }));
      onStatusChange?.(status);
    },
    [onStatusChange]
  );

  const connect = useCallback(
    (url: string, params?: Record<string, string | number | boolean>) => {
      const urlObj = new URL(url, window.location.origin);

      const token = localStorage.getItem('satoken');
      if (token) {
        urlObj.searchParams.set('satoken', token);
      }

      if (params) {
        Object.entries(params).forEach(([k, v]) => {
          if (v !== undefined && v !== null) {
            urlObj.searchParams.set(k, String(v));
          }
        });
      }

      if (esRef.current) {
        esRef.current.close();
      }

      setStatus('connecting');
      retryCountRef.current = 0;

      const es = new EventSource(urlObj.toString());
      esRef.current = es;

      es.onopen = () => {
        setStatus('connected');
        setState((prev) => ({ ...prev, error: null }));
      };

      const eventNames: SSEEventName[] = [
        'intent',
        'rewrite',
        'retrieval',
        'guidance',
        'thinking',
        'reasoning',
        'answer',
        'done',
        'error',
        'tool_start',
        'tool_end',
      ];

      eventNames.forEach((name) => {
        es.addEventListener(name, (e: MessageEvent) => {
          let data: unknown;
          try {
            data = JSON.parse(e.data);
          } catch {
            data = e.data;
          }
          setState((prev) => ({ ...prev, lastEvent: { name, data } }));
          handlersRef.current.get(name)?.(data);
          if (name === 'done') {
            es.close();
            esRef.current = null;
            setStatus('closed');
            setState((prev) => ({ ...prev, error: null }));
          }
        });
      });

      es.onerror = () => {
        if (es.readyState === EventSource.CLOSED) {
          setStatus('closed');
          return;
        }
        setStatus('error');
        const errMsg = 'SSE 连接错误或后端未启动';

        if (autoReconnect && retryCountRef.current < maxRetries) {
          retryCountRef.current += 1;
          setState((prev) => ({
            ...prev,
            error: `${errMsg}，${retryCountRef.current}/${maxRetries} 次重连中...`,
          }));
          retryTimerRef.current = setTimeout(() => {
            connect(url, params);
          }, retryDelay * retryCountRef.current);
        } else {
          setState((prev) => ({ ...prev, error: errMsg + '，已达最大重试次数' }));
        }
      };
    },
    [autoReconnect, maxRetries, retryDelay, setStatus]
  );

  const disconnect = useCallback(() => {
    if (retryTimerRef.current) {
      clearTimeout(retryTimerRef.current);
    }
    if (esRef.current) {
      esRef.current.close();
      esRef.current = null;
    }
    setStatus('closed');
  }, [setStatus]);

  const on = useCallback((eventName: SSEEventName, handler: (data: unknown) => void) => {
    handlersRef.current.set(eventName, handler);
    return () => handlersRef.current.delete(eventName);
  }, []);

  const off = useCallback((eventName: SSEEventName) => {
    handlersRef.current.delete(eventName);
  }, []);

  return {
    state,
    connect,
    disconnect,
    on,
    off,
  };
}
