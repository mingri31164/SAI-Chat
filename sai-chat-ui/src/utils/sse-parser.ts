import type {
  SSEIntentEvent,
  SSERewriteEvent,
  SSERetrievalEvent,
  SSEToolStartEvent,
  SSEToolEndEvent,
  SSEThinkingEvent,
  SSEDoneEvent,
  SSEErrorEvent,
  SSEIntentEvent_Agent,
  SSEDoneEvent_Agent,
} from '@/types/agent';

export type SSEEventName =
  | 'intent'
  | 'rewrite'
  | 'retrieval'
  | 'guidance'
  | 'thinking'
  | 'reasoning'
  | 'answer'
  | 'done'
  | 'error'
  | 'tool_start'
  | 'tool_end'
  | 'open';

export interface ParsedSSEEvent<T = unknown> {
  eventName: SSEEventName;
  data: T;
  raw: string;
}

export function parseSSEEvent(raw: string): ParsedSSEEvent | null {
  const trimmed = raw.trim();
  if (!trimmed) return null;

  // Format: "event: name\ndata: {...}\n\n" or "data: {...}"
  const eventMatch = trimmed.match(/^event:\s*(\w+)\s*$/m);
  const dataMatch = trimmed.match(/^data:\s*(.+)$/ms);

  if (!dataMatch) return null;

  const eventName = (eventMatch ? eventMatch[1] : 'message') as SSEEventName;
  const rawData = dataMatch[1];

  let data: unknown;
  try {
    data = JSON.parse(rawData);
  } catch {
    // Some events (like 'answer') may send plain text
    data = rawData;
  }

  return { eventName, data, raw: rawData };
}

export function parseIntentEvent(raw: string): SSEIntentEvent | null {
  try {
    return JSON.parse(raw) as SSEIntentEvent;
  } catch {
    return null;
  }
}

export function parseRewriteEvent(raw: string): SSERewriteEvent | null {
  try {
    return JSON.parse(raw) as SSERewriteEvent;
  } catch {
    return null;
  }
}

export function parseRetrievalEvent(raw: string): SSERetrievalEvent | null {
  try {
    return JSON.parse(raw) as SSERetrievalEvent;
  } catch {
    return null;
  }
}

export function parseToolStartEvent(raw: string): SSEToolStartEvent | null {
  try {
    return JSON.parse(raw) as SSEToolStartEvent;
  } catch {
    return null;
  }
}

export function parseToolEndEvent(raw: string): SSEToolEndEvent | null {
  try {
    return JSON.parse(raw) as SSEToolEndEvent;
  } catch {
    return null;
  }
}

export function parseThinkingEvent(raw: string): SSEThinkingEvent | null {
  try {
    return JSON.parse(raw) as SSEThinkingEvent;
  } catch {
    return null;
  }
}

export function parseDoneEvent(raw: string): SSEDoneEvent | null {
  try {
    return JSON.parse(raw) as SSEDoneEvent;
  } catch {
    return null;
  }
}

export function parseErrorEvent(raw: string): SSEErrorEvent | null {
  try {
    return JSON.parse(raw) as SSEErrorEvent;
  } catch {
    return null;
  }
}

export function parseAgentDoneEvent(raw: string): SSEDoneEvent_Agent | null {
  try {
    return JSON.parse(raw) as SSEDoneEvent_Agent;
  } catch {
    return null;
  }
}

export function parseAgentIntentEvent(raw: string): SSEIntentEvent_Agent | null {
  try {
    return JSON.parse(raw) as SSEIntentEvent_Agent;
  } catch {
    return null;
  }
}
