import { useEffect, useRef, useState } from 'react';
import {
  Card,
  Input,
  Button,
  Space,
  Typography,
  Switch,
  Segmented,
  Divider,
  Badge,
  Tag,
  message,
  Slider,
} from 'antd';
import {
  ExperimentOutlined,
  SendOutlined,
  ThunderboltOutlined,
  ClearOutlined,
  StopOutlined,
  RobotOutlined,
  UserOutlined,
} from '@ant-design/icons';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import { useChatStore } from '@/stores/chatStore';
import { useRAGStream, useAgentStream } from '@/hooks/useStream';
import { useConversation } from '@/hooks/useConversation';
import StreamPanel from './StreamPanel';
import ToolCallPanel from './ToolCallPanel';
import PipelineStage from './PipelineStage';

const { Text, Title } = Typography;
const { TextArea } = Input;

export default function Chat() {
  const {
    currentSession,
    currentSessionId,
    chatMode,
    deepThinking,
    maxIterations,
    createSession,
    clearSession,
  } = useConversation();

  // Auto-create a session on mount
  useEffect(() => {
    if (!currentSessionId) {
      createSession();
    }
  }, []);

  const ragStream = useRAGStream(currentSessionId || '');
  const agentStream = useAgentStream(currentSessionId || '');
  const [inputValue, setInputValue] = useState('');
  const bottomRef = useRef<HTMLDivElement>(null);
  const inputRef = useRef<any>(null);

  const currentStream = chatMode === 'RAG' ? ragStream : agentStream;
  const isStreaming = currentSession?.isStreaming ?? false;

  // Auto-scroll to bottom
  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [currentSession?.messages, currentSession?.streamEvents]);

  const handleSend = async () => {
    if (!inputValue.trim() || isStreaming) return;

    const question = inputValue.trim();
    setInputValue('');
    inputRef.current?.focus();

    if (chatMode === 'RAG') {
      ragStream.startStream(question, 'guest', deepThinking);
    } else {
      agentStream.startStream(question, 'guest', deepThinking, maxIterations);
    }
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  };

  const handleClear = () => {
    if (currentSessionId) {
      clearSession(currentSessionId);
      message.success('会话已清空');
    }
  };

  const handleStop = () => {
    currentStream.stopStream();
  };

  const pipelineStages =
    chatMode === 'RAG'
      ? [
          { key: 'intent', label: '意图识别', icon: '🎯' },
          { key: 'rewrite', label: '问题改写', icon: '✏️' },
          { key: 'retrieval', label: '多路检索', icon: '🔍' },
          { key: 'answer', label: 'LLM 生成', icon: '💬' },
        ]
      : [
          { key: 'thinking', label: '思考 (THINK)', icon: '🧠' },
          { key: 'executing', label: '执行 (ACT)', icon: '⚡' },
          { key: 'observing', label: '观察 (OBSERVE)', icon: '👁️' },
        ];

  const messages = currentSession?.messages || [];
  const currentStage = currentSession?.currentStage;
  const toolCalls = currentSession?.toolCalls || [];

  return (
    <div className="flex gap-4 h-[calc(100vh-140px)]">
      {/* Main chat area */}
      <div className="flex-1 flex flex-col gap-3">
        {/* Mode selector + settings */}
        <Card size="small" className="flex-shrink-0">
          <div className="flex items-center justify-between flex-wrap gap-3">
            <Space>
              <Segmented
                value={chatMode}
                onChange={(v) => {
                  useChatStore.getState().setChatMode(v as 'RAG' | 'Agent');
                }}
                options={[
                  {
                    label: (
                      <Space size={4}>
                        <ThunderboltOutlined />
                        <span>RAG 检索</span>
                      </Space>
                    ),
                    value: 'RAG',
                  },
                  {
                    label: (
                      <Space size={4}>
                        <ExperimentOutlined />
                        <span>ReAct Agent</span>
                      </Space>
                    ),
                    value: 'Agent',
                  },
                ]}
              />

              <Divider type="vertical" />

              <Space size={4}>
                <Switch
                  checked={deepThinking}
                  onChange={(v) => useChatStore.getState().setDeepThinking(v)}
                  size="small"
                />
                <Text type="secondary" className="text-xs">深思考</Text>
              </Space>

              {chatMode === 'Agent' && (
                <Space size={4}>
                  <Text type="secondary" className="text-xs">最大迭代:</Text>
                  <Slider
                    min={1}
                    max={20}
                    value={maxIterations}
                    onChange={(v: number) => useChatStore.getState().setMaxIterations(v)}
                    style={{ width: 80 }}
                  />
                  <Text className="text-xs">{maxIterations}</Text>
                </Space>
              )}
            </Space>

            <Space>
              {isStreaming ? (
                <Button
                  danger
                  size="small"
                  icon={<StopOutlined />}
                  onClick={handleStop}
                >
                  停止生成
                </Button>
              ) : (
                <Button
                  size="small"
                  icon={<ClearOutlined />}
                  onClick={handleClear}
                  disabled={messages.length === 0}
                >
                  清空会话
                </Button>
              )}
            </Space>
          </div>
        </Card>

        {/* Pipeline stages */}
        {isStreaming && (
          <PipelineStage
            stages={pipelineStages}
            currentStage={currentStage}
            chatMode={chatMode}
          />
        )}

        {/* Messages */}
        <Card
          className="flex-1 overflow-y-auto"
          bodyStyle={{ padding: 0, height: '100%' }}
          styles={{ body: { height: '100%', overflowY: 'auto', padding: '16px' } }}
        >
          {messages.length === 0 && !isStreaming ? (
            <div className="flex flex-col items-center justify-center h-full text-center">
              <RobotOutlined style={{ fontSize: 48, color: '#d9d9d9' }} />
              <Title level={5} type="secondary" className="mt-4">
                {chatMode === 'RAG' ? 'RAG 智能问答' : 'ReAct Agent'}
              </Title>
              <Text type="secondary">
                {chatMode === 'RAG'
                  ? '发送问题，体验多路检索 · 意图识别 · 问题改写'
                  : '发送问题，体验思考-执行-观察循环 · MCP 工具调用'}
              </Text>
            </div>
          ) : (
            <div className="space-y-4 pb-16">
              {messages.map((msg, i) => (
                <MessageBubble key={i} message={msg} />
              ))}
              {isStreaming && messages[messages.length - 1]?.role === 'assistant' && (
                <div className="text-sm text-gray-400">正在生成回答...</div>
              )}
            </div>
          )}
          <div ref={bottomRef} />
        </Card>

        {/* Input area */}
        <Card size="small" className="flex-shrink-0">
          <div className="flex gap-2 items-end">
            <TextArea
              ref={inputRef}
              value={inputValue}
              onChange={(e) => setInputValue(e.target.value)}
              onKeyDown={handleKeyDown}
              placeholder={
                isStreaming
                  ? '正在生成回答中...'
                  : chatMode === 'RAG'
                  ? '输入问题，体验 RAG 检索增强生成...'
                  : '输入问题，体验 ReAct Agent 智能体...'
              }
              autoSize={{ minRows: 1, maxRows: 4 }}
              disabled={isStreaming}
              className="flex-1"
            />
            <Button
              type="primary"
              icon={<SendOutlined />}
              onClick={handleSend}
              loading={isStreaming}
              disabled={!inputValue.trim() && !isStreaming}
            >
              {isStreaming ? '生成中' : '发送'}
            </Button>
          </div>
          <div className="flex gap-4 mt-2">
            <Text type="secondary" className="text-xs">
              Shift+Enter 换行 · Enter 发送
            </Text>
            {currentSession?.stats && (
              <Text type="secondary" className="text-xs ml-auto">
                {currentSession.stats.durationMs}ms · {currentSession.stats.totalTokens} tokens
              </Text>
            )}
          </div>
        </Card>
      </div>

      {/* Right sidebar */}
      <div className="w-80 flex-shrink-0 flex flex-col gap-3 overflow-y-auto">
        {/* Stream detail panel */}
        <StreamPanel session={currentSession} chatMode={chatMode} />

        {/* Tool call panel */}
        {chatMode === 'Agent' && (
          <ToolCallPanel toolCalls={toolCalls} />
        )}

        {/* Memory info */}
        {currentSession && (
          <Card size="small" title="会话记忆">
            <div className="space-y-2">
              <div className="flex justify-between">
                <Text type="secondary" className="text-xs">会话 ID</Text>
                <Text code className="text-xs">{currentSession.sessionId.slice(-12)}</Text>
              </div>
              <div className="flex justify-between">
                <Text type="secondary" className="text-xs">消息数</Text>
                <Text className="text-xs">{currentSession.messages.length}</Text>
              </div>
              <div className="flex justify-between">
                <Text type="secondary" className="text-xs">状态</Text>
                <Tag
                  color={
                    currentSession.agentStatus === 'COMPLETED'
                      ? 'green'
                      : currentSession.agentStatus === 'FAILED'
                      ? 'red'
                      : 'blue'
                  }
                  className="text-xs"
                >
                  {currentSession.agentStatus}
                </Tag>
              </div>
              {currentSession.stats && (
                <>
                  <Divider className="my-2" />
                  <div className="flex justify-between">
                    <Text type="secondary" className="text-xs">耗时</Text>
                    <Text className="text-xs">{currentSession.stats.durationMs}ms</Text>
                  </div>
                  <div className="flex justify-between">
                    <Text type="secondary" className="text-xs">Token</Text>
                    <Text className="text-xs">{currentSession.stats.totalTokens}</Text>
                  </div>
                  {currentSession.stats.totalIterations > 0 && (
                    <div className="flex justify-between">
                      <Text type="secondary" className="text-xs">迭代次数</Text>
                      <Text className="text-xs">{currentSession.stats.totalIterations}</Text>
                    </div>
                  )}
                </>
              )}
            </div>
          </Card>
        )}

        {/* Connection status */}
        <Card size="small">
          <div className="flex items-center gap-2">
            <Badge
              status={currentStream.status === 'connected' ? 'success' : currentStream.status === 'error' ? 'error' : 'default'}
              text={
                <Text type="secondary" className="text-xs">
                  {currentStream.status === 'connected' && '已连接'}
                  {currentStream.status === 'connecting' && '连接中...'}
                  {currentStream.status === 'error' && '连接错误'}
                  {currentStream.status === 'idle' && '未连接'}
                  {currentStream.status === 'closed' && '已断开'}
                </Text>
              }
            />
          </div>
          {currentStream.error && (
            <Text type="danger" className="text-xs mt-1 block">
              {currentStream.error}
            </Text>
          )}
        </Card>
      </div>
    </div>
  );
}

function MessageBubble({ message }: { message: { role: string; content: string; timestamp: number } }) {
  const isUser = message.role === 'user';
  return (
    <div className={`flex gap-3 ${isUser ? 'flex-row-reverse' : ''}`}>
      <div
        className={`w-8 h-8 rounded-full flex items-center justify-center flex-shrink-0 ${
          isUser ? 'bg-blue-500' : 'bg-gray-200'
        }`}
      >
        {isUser ? (
          <UserOutlined style={{ color: 'white', fontSize: 14 }} />
        ) : (
          <RobotOutlined style={{ color: '#666', fontSize: 14 }} />
        )}
      </div>
      <div
        className={`max-w-[75%] rounded-2xl px-4 py-2 ${
          isUser
            ? 'bg-blue-500 text-white'
            : 'bg-gray-100 text-gray-800'
        }`}
      >
        <div className="markdown-body text-sm">
          <ReactMarkdown remarkPlugins={[remarkGfm]}>{message.content}</ReactMarkdown>
        </div>
        <Text
          type={isUser ? undefined : 'secondary'}
          className={`text-xs block mt-1 ${isUser ? 'text-blue-100' : ''}`}
        >
          {new Date(message.timestamp).toLocaleTimeString('zh-CN', {
            hour: '2-digit',
            minute: '2-digit',
          })}
        </Text>
      </div>
    </div>
  );
}
