/**
 * Format a millisecond duration into a human-readable string
 */
export function formatDuration(ms: number): string {
  if (ms < 1000) return `${ms}ms`;
  if (ms < 60000) return `${(ms / 1000).toFixed(1)}s`;
  const minutes = Math.floor(ms / 60000);
  const seconds = Math.floor((ms % 60000) / 1000);
  return `${minutes}m ${seconds}s`;
}

/**
 * Format timestamp to local time string
 */
export function formatTime(timestamp: number): string {
  return new Date(timestamp).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
  });
}

/**
 * Format timestamp to relative time (e.g., "2分钟前")
 */
export function formatRelativeTime(timestamp: number): string {
  const now = Date.now();
  const diff = now - timestamp;
  if (diff < 60000) return '刚刚';
  if (diff < 3600000) return `${Math.floor(diff / 60000)} 分钟前`;
  if (diff < 86400000) return `${Math.floor(diff / 3600000)} 小时前`;
  return `${Math.floor(diff / 86400000)} 天前`;
}

/**
 * Format token count with comma separator
 */
export function formatTokens(tokens: number): string {
  return tokens.toLocaleString('zh-CN');
}

/**
 * Format cost in CNY
 */
export function formatCost(yuan: number): string {
  if (yuan < 0.01) return '0.00 元';
  return `${yuan.toFixed(yuan < 1 ? 4 : 2)} 元`;
}

/**
 * Format a percentage value (0-1) to "XX%"
 */
export function formatPercent(value: number, decimals = 1): string {
  return `${(value * 100).toFixed(decimals)}%`;
}

/**
 * Format a score (0-100 or 0-1) to fixed decimals
 */
export function formatScore(value: number, max = 100): string {
  if (value > 1) return value.toFixed(0);
  return ((value / max) * 100).toFixed(decimals(value));
}

/**
 * Determine decimal places based on value magnitude
 */
function decimals(value: number): number {
  if (value >= 10) return 0;
  if (value >= 1) return 1;
  return 2;
}

/**
 * Truncate text to a given length with ellipsis
 */
export function truncate(text: string, maxLength = 100): string {
  if (text.length <= maxLength) return text;
  return text.slice(0, maxLength) + '...';
}

/**
 * Get score color for gradient display
 */
export function getScoreColor(score: number): string {
  if (score >= 0.8) return '#52c41a'; // green
  if (score >= 0.6) return '#faad14'; // yellow
  if (score >= 0.4) return '#fa8c16'; // orange
  return '#ff4d4f'; // red
}

/**
 * Get status badge color for AgentStatus
 */
export function getAgentStatusColor(status: string): string {
  const colors: Record<string, string> = {
    IDLE: '#8c8c8c',
    THINKING: '#1677ff',
    PLANNING: '#722ed1',
    EXECUTING: '#fa8c16',
    OBSERVING: '#13c2c2',
    REFLECTING: '#08979c',
    COMPLETED: '#52c41a',
    FAILED: '#ff4d4f',
    WAITING: '#fa8c16',
    EXCEEDED: '#faad14',
    RUNNING: '#1677ff',
    PAUSED: '#faad14',
    DRAFT: '#8c8c8c',
    CANCELLED: '#8c8c8c',
  };
  return colors[status] || '#8c8c8c';
}

/**
 * Generate a simple UUID
 */
export function generateId(): string {
  return `${Date.now().toString(36)}-${Math.random().toString(36).slice(2, 9)}`;
}

/**
 * Deep clone an object
 */
export function deepClone<T>(obj: T): T {
  return JSON.parse(JSON.stringify(obj));
}
