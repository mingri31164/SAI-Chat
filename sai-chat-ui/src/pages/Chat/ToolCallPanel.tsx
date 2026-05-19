import {
  Card, Timeline, Typography, Tag, Space, Tooltip, Badge
} from 'antd';
import { CheckCircleFilled, CloseCircleFilled, LoadingOutlined } from '@ant-design/icons';
import type { ToolCallRecord } from '@/types/agent';
import { formatDuration } from '@/utils/format';

const { Text } = Typography;

interface Props {
  toolCalls: ToolCallRecord[];
}

const toolColors: Record<string, string> = {
  sales_query: 'blue',
  weather_query: 'cyan',
  ticket_query: 'purple',
};

export default function ToolCallPanel({ toolCalls }: Props) {
  if (toolCalls.length === 0) {
    return (
      <Card size="small" title="工具调用">
        <Text type="secondary" className="text-xs">暂无工具调用记录</Text>
      </Card>
    );
  }

  return (
    <Card
      size="small"
      title={
        <Space>
          <span>工具调用</span>
          <Badge count={toolCalls.length} style={{ backgroundColor: '#3b82f6' }} />
        </Space>
      }
      className="flex-1 overflow-y-auto"
    >
      <Timeline
        items={toolCalls.map((tc, idx) => {
          const color = toolColors[tc.toolId] || 'blue';
          const duration = tc.endTime && tc.startTime ? tc.endTime - tc.startTime : undefined;

          return {
            key: idx,
            dot: tc.success === false ? (
              <CloseCircleFilled style={{ color: '#ff4d4f' }} />
            ) : tc.success === true ? (
              <CheckCircleFilled style={{ color: '#52c41a' }} />
            ) : (
              <LoadingOutlined />
            ),
            children: (
              <div className="space-y-1">
                <div className="flex items-center gap-2">
                  <Tag color={color} className="text-xs">
                    步骤 {tc.step}
                  </Tag>
                  <Text strong className="text-xs">{tc.toolName || tc.toolId}</Text>
                  {duration !== undefined && (
                    <Text type="secondary" className="text-xs">
                      {formatDuration(duration)}
                    </Text>
                  )}
                </div>

                {Object.keys(tc.parameters || {}).length > 0 && (
                  <Tooltip title={JSON.stringify(tc.parameters, null, 2)}>
                    <div className="bg-gray-100 rounded px-2 py-1 text-xs cursor-pointer">
                      <Text type="secondary">参数: </Text>
                      <Text className="text-xs">
                        {JSON.stringify(tc.parameters).slice(0, 50)}
                        {JSON.stringify(tc.parameters).length > 50 ? '...' : ''}
                      </Text>
                    </div>
                  </Tooltip>
                )}

                {tc.result && (
                  <details className="text-xs">
                    <summary className="cursor-pointer text-blue-500 text-xs">
                      查看结果
                    </summary>
                    <pre className="bg-gray-50 rounded p-2 mt-1 text-xs overflow-x-auto max-h-32">
                      {tc.result.slice(0, 300)}
                      {tc.result.length > 300 && '...'}
                    </pre>
                  </details>
                )}

                {tc.error && (
                  <Text type="danger" className="text-xs block">
                    错误: {tc.error}
                  </Text>
                )}
              </div>
            ),
          };
        })}
      />
    </Card>
  );
}
