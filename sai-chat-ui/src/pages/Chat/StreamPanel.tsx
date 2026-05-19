import {
  Card, Typography, Tag, Space, Divider, Progress
} from 'antd';
import { getAgentStatusColor, getScoreColor } from '@/utils/format';
import type { ChatSession } from '@/stores/chatStore';
import type { NodeScore } from '@/types/agent';
import { IntentKind } from '@/types/agent';
import type { RetrievedChunk } from '@/types/agent';

const { Text } = Typography;

interface Props {
  session: ChatSession | null;
  chatMode: 'RAG' | 'Agent';
}

export default function StreamPanel({ session, chatMode }: Props) {
  if (!session) {
    return (
      <Card size="small" title="流式详情">
        <Text type="secondary" className="text-xs">暂无数据</Text>
      </Card>
    );
  }

  const intentScores = (session.intentScores || []) as NodeScore[];
  const rewriteResult = session.rewriteResult;
  const retrievedChunks = (session.retrievedChunks || []) as RetrievedChunk[];

  return (
    <Card size="small" title="流式详情" className="flex-1 overflow-y-auto">
      <div className="space-y-4">
        {/* Intent Classification */}
        {chatMode === 'RAG' && intentScores.length > 0 && (
          <div>
            <Space>
              <Text strong className="text-xs">意图识别</Text>
              <Tag color="blue">{intentScores[0]?.score?.toFixed(2)}</Tag>
            </Space>
            <div className="mt-2 space-y-1">
              {intentScores.slice(0, 5).map((ns) => (
                <div key={ns.id} className="flex items-center gap-2">
                  <Progress
                    percent={Math.round(ns.score * 100)}
                    size="small"
                    strokeColor={getScoreColor(ns.score)}
                    className="flex-1"
                    format={() => ''}
                  />
                  <Text className="text-xs w-24 truncate" title={ns.path}>
                    {ns.name}
                  </Text>
                  {ns.kind && (
                    <Tag
                      color={
                        ns.kind === IntentKind.KB
                          ? 'blue'
                          : ns.kind === IntentKind.MCP
                          ? 'orange'
                          : 'default'
                      }
                      className="text-xs"
                    >
                      {ns.kind}
                    </Tag>
                  )}
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Rewrite Result */}
        {chatMode === 'RAG' && rewriteResult && (
          <div>
            <Text strong className="text-xs block mb-2">问题改写</Text>
            {rewriteResult.rewrittenQuestion && (
              <div className="bg-blue-50 rounded p-2 text-xs">
                <Text type="secondary">改写: </Text>
                <Text>{rewriteResult.rewrittenQuestion}</Text>
              </div>
            )}
            {rewriteResult.subQuestions && rewriteResult.subQuestions.length > 0 && (
              <div className="mt-1">
                <Text type="secondary" className="text-xs">子问题: </Text>
                {rewriteResult.subQuestions.map((sq, i) => (
                  <Tag key={i} className="text-xs mx-0.5">{sq}</Tag>
                ))}
              </div>
            )}
          </div>
        )}

        {/* Retrieval Results */}
        {chatMode === 'RAG' && retrievedChunks.length > 0 && (
          <div>
            <Space>
              <Text strong className="text-xs">检索结果</Text>
              <Tag>{retrievedChunks.length} 个片段</Tag>
            </Space>
            <div className="mt-2 space-y-2 max-h-60 overflow-y-auto">
              {retrievedChunks.map((chunk, i) => (
                <Card
                  key={chunk.id || i}
                  size="small"
                  className="text-xs"
                  bodyStyle={{ padding: 8 }}
                >
                  <div className="flex justify-between items-start mb-1">
                    <Text type="secondary" className="text-xs truncate flex-1">
                      {chunk.id || `片段 ${i + 1}`}
                    </Text>
                    <Tag
                      color={getScoreColor(chunk.score)}
                      className="text-xs ml-1"
                    >
                      {(chunk.score * 100).toFixed(0)}%
                    </Tag>
                  </div>
                  <Text className="text-xs line-clamp-3" style={{ display: '-webkit-box', WebkitLineClamp: 3, WebkitBoxOrient: 'vertical', overflow: 'hidden' }}>
                    {chunk.text}
                  </Text>
                </Card>
              ))}
            </div>
          </div>
        )}

        {/* Agent status */}
        {chatMode === 'Agent' && (
          <div>
            <Space>
              <Text strong className="text-xs">执行状态</Text>
              <Tag
                color={getAgentStatusColor(session.agentStatus)}
                style={{ color: 'white' }}
              >
                {session.agentStatus}
              </Tag>
            </Space>
            {session.agentStatus === 'EXECUTING' && (
              <div className="mt-2">
                <Tag color="orange">工具执行中</Tag>
              </div>
            )}
            {session.agentStatus === 'OBSERVING' && (
              <div className="mt-2">
                <Tag color="cyan">反思评估中</Tag>
              </div>
            )}
            {session.agentStatus === 'WAITING' && (
              <div className="mt-2">
                <Tag color="blue">等待用户输入</Tag>
              </div>
            )}
          </div>
        )}

        {/* Stream events timeline */}
        {session.streamEvents.length > 0 && (
          <div>
            <Divider className="my-2" />
            <Text type="secondary" className="text-xs block mb-2">事件流</Text>
            <div className="space-y-1 max-h-40 overflow-y-auto">
              {session.streamEvents.slice(-20).map((ev, i) => (
                <div key={i} className="flex items-start gap-2 text-xs">
                  <Tag className="text-xs flex-shrink-0">{ev.type}</Tag>
                  <Text type="secondary" className="truncate flex-1">
                    {typeof ev.data === 'string'
                      ? (ev.data as string).slice(0, 60)
                      : JSON.stringify(ev.data).slice(0, 60)}
                  </Text>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>
    </Card>
  );
}
