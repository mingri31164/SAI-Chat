import { Card, Row, Col, Statistic, Typography, Space, Tag, Badge, List, Progress, Spin } from 'antd';
import {
  LineChartOutlined,
  CheckCircleOutlined,
  ClockCircleOutlined,
  DollarOutlined,
  ThunderboltOutlined,
  MessageOutlined,
  RobotOutlined,
  ExclamationCircleOutlined,
} from '@ant-design/icons';
import { useMetrics } from '@/hooks/useMetrics';
import { useObserveStore } from '@/stores/observeStore';
import { formatDuration, formatTime, formatPercent, getAgentStatusColor } from '@/utils/format';

const { Title, Text } = Typography;

export default function Dashboard() {
  const { metricsSnapshot, recentTraces, refresh: _r } = useMetrics();
  const { isLoading, error } = useObserveStore();

  if (isLoading && !metricsSnapshot) {
    return (
      <div className="flex items-center justify-center h-96">
        <Spin size="large" tip="加载监控数据..." />
      </div>
    );
  }

  if (error && !metricsSnapshot) {
    return (
      <Card>
        <div className="text-center py-12">
          <ExclamationCircleOutlined style={{ fontSize: 48, color: '#ff4d4f' }} />
          <Title level={4} className="mt-4">无法加载监控数据</Title>
          <Text type="secondary">{error}</Text>
          <Text type="secondary" className="block mt-2">
            请确保后端服务 (chat-agent) 已启动于 localhost:8080
          </Text>
        </div>
      </Card>
    );
  }

  const m = metricsSnapshot;

  return (
    <div className="space-y-4">
      <Title level={4} className="mb-4">实时监控</Title>

      {/* Metric Cards */}
      <Row gutter={[16, 16]}>
        <Col xs={24} sm={12} lg={6}>
          <Card className="card-hover">
            <Statistic
              title="成功率"
              value={m ? formatPercent(m.successRate) : '—'}
              prefix={<CheckCircleOutlined style={{ color: '#52c41a' }} />}
              suffix={
                m && (
                  <span className="text-xs ml-1">
                    <Text type="secondary">{m.successRequests}/{m.totalRequests}</Text>
                  </span>
                )
              }
              valueStyle={{ color: '#52c41a' }}
            />
            <Progress
              percent={m ? Math.round(m.successRate * 100) : 0}
              showInfo={false}
              strokeColor="#52c41a"
              size="small"
              className="mt-2"
            />
          </Card>
        </Col>

        <Col xs={24} sm={12} lg={6}>
          <Card className="card-hover">
            <Statistic
              title="QPS (每秒请求)"
              value={m ? m.qps.toFixed(2) : '—'}
              prefix={<ThunderboltOutlined style={{ color: '#3b82f6' }} />}
              valueStyle={{ color: '#3b82f6' }}
            />
            <Text type="secondary" className="text-xs mt-2 block">
              总请求: {m?.totalRequests || 0}
            </Text>
          </Card>
        </Col>

        <Col xs={24} sm={12} lg={6}>
          <Card className="card-hover">
            <Statistic
              title="P95 延迟"
              value={m ? formatDuration(m.p95LatencyMs) : '—'}
              prefix={<ClockCircleOutlined style={{ color: '#f59e0b' }} />}
              valueStyle={{ color: m && m.p95LatencyMs > 3000 ? '#ff4d4f' : '#f59e0b' }}
            />
            <div className="flex gap-4 mt-2">
              <Text type="secondary" className="text-xs">P50: {m ? formatDuration(m.p50LatencyMs) : '—'}</Text>
              <Text type="secondary" className="text-xs">P99: {m ? formatDuration(m.p99LatencyMs) : '—'}</Text>
            </div>
          </Card>
        </Col>

        <Col xs={24} sm={12} lg={6}>
          <Card className="card-hover">
            <Statistic
              title="Token 消耗"
              value={m ? m.totalTokens.toLocaleString() : '—'}
              prefix={<MessageOutlined style={{ color: '#8b5cf6' }} />}
              suffix="tokens"
              valueStyle={{ color: '#8b5cf6' }}
            />
            <Text type="secondary" className="text-xs mt-2 block">
              均值: {m ? Math.round(m.averageTokensPerRequest) : 0} tokens/请求
            </Text>
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]}>
        <Col xs={24} sm={12} lg={6}>
          <Card className="card-hover">
            <Statistic
              title="总费用 (CNY)"
              value={m ? `¥${m.totalCostYuan.toFixed(4)}` : '—'}
              prefix={<DollarOutlined style={{ color: '#ef4444' }} />}
              valueStyle={{ color: '#ef4444' }}
            />
          </Card>
        </Col>

        <Col xs={24} sm={12} lg={6}>
          <Card className="card-hover">
            <Statistic
              title="LLM 调用次数"
              value={m?.llmCalls || 0}
              prefix={<RobotOutlined style={{ color: '#10b981' }} />}
            />
            <Text type="secondary" className="text-xs mt-2 block">
              均值延迟: {m ? formatDuration(m.averageLlmDurationMs) : '—'}
            </Text>
          </Card>
        </Col>

        <Col xs={24} sm={12} lg={6}>
          <Card className="card-hover">
            <Statistic
              title="工具调用次数"
              value={m?.toolCalls || 0}
              prefix={<RobotOutlined style={{ color: '#f97316' }} />}
            />
            <Text type="secondary" className="text-xs mt-2 block">
              均值延迟: {m ? formatDuration(m.averageToolDurationMs) : '—'}
            </Text>
          </Card>
        </Col>

        <Col xs={24} sm={12} lg={6}>
          <Card className="card-hover">
            <Statistic
              title="活跃会话数"
              value={m?.activeSessions || 0}
              prefix={<MessageOutlined style={{ color: '#06b6d4' }} />}
            />
            <Text type="secondary" className="text-xs mt-2 block">
              超时率: {m ? formatPercent(m.timeoutRate) : '—'}
            </Text>
          </Card>
        </Col>
      </Row>

      {/* Recent traces */}
      <Row gutter={[16, 16]}>
        <Col xs={24} lg={16}>
          <Card
            title={
              <Space>
                <LineChartOutlined />
                <span>最近调用链路</span>
                {isLoading && <Badge status="processing" text="刷新中" />}
              </Space>
            }
            extra={<Text type="secondary">{formatTime(Date.now())}</Text>}
          >
            <List
              size="small"
              dataSource={recentTraces.slice(0, 10)}
              locale={{ emptyText: '暂无调用记录' }}
              renderItem={(trace) => (
                <List.Item
                  actions={[
                    <Text key="duration" type="secondary" className="text-xs">
                      {formatDuration(trace.totalDurationMs || 0)}
                    </Text>,
                  ]}
                >
                  <List.Item.Meta
                    title={
                      <Space>
                        <Badge
                          color={getAgentStatusColor(trace.status)}
                          text={<span className="text-sm">{trace.traceId}</span>}
                        />
                        <Tag>{trace.sessionId.slice(-8)}</Tag>
                      </Space>
                    }
                    description={
                      <Space size="small">
                        <Text type="secondary" className="text-xs">
                          {trace.userId}
                        </Text>
                        <Text type="secondary" className="text-xs">
                          {trace.spanList.length} spans
                        </Text>
                        <Text type="secondary" className="text-xs">
                          {trace.startTimeMs ? formatTime(trace.startTimeMs) : '—'}
                        </Text>
                      </Space>
                    }
                  />
                </List.Item>
              )}
            />
          </Card>
        </Col>

        <Col xs={24} lg={8}>
          <Card title={<Space><RobotOutlined /><span>错误分析</span></Space>}>
            <div className="space-y-3">
              <div className="flex justify-between items-center">
                <Text>失败请求</Text>
                <Tag color="red">{m?.failedRequests || 0}</Tag>
              </div>
              <div className="flex justify-between items-center">
                <Text>超时请求</Text>
                <Tag color="orange">{m?.timeoutRequests || 0}</Tag>
              </div>
              <div className="flex justify-between items-center">
                <Text>错误率</Text>
                <Tag color={m && m.errorRate > 0.1 ? 'red' : 'green'}>
                  {m ? formatPercent(m.errorRate) : '—'}
                </Tag>
              </div>
              <div className="flex justify-between items-center">
                <Text>LLM 调用</Text>
                <Tag color="blue">{m?.llmCalls || 0}</Tag>
              </div>
              <div className="flex justify-between items-center">
                <Text>工具调用</Text>
                <Tag color="purple">{m?.toolCalls || 0}</Tag>
              </div>
            </div>
          </Card>
        </Col>
      </Row>
    </div>
  );
}
