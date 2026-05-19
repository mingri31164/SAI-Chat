import React from 'react';
import { Typography, Space, Tag } from 'antd';

const { Text } = Typography;

interface Stage {
  key: string;
  label: string;
  icon: string;
}

interface Props {
  stages: Stage[];
  currentStage?: string;
  chatMode: 'RAG' | 'Agent';
}

const statusColorMap: Record<string, string> = {
  IDLE: 'gray',
  THINKING: 'blue',
  PLANNING: 'purple',
  EXECUTING: 'orange',
  OBSERVING: 'cyan',
  REFLECTING: 'teal',
  COMPLETED: 'green',
  FAILED: 'red',
  EXCEEDED: 'gold',
};

const ragStageMap: Record<string, number> = {
  intent: 0,
  rewrite: 1,
  retrieval: 2,
  answer: 3,
  done: 3,
  thinking: 1,
  executing: 2,
  observing: 3,
};

const agentStageMap: Record<string, string> = {
  thinking: 'THINKING',
  executing: 'EXECUTING',
  observing: 'OBSERVING',
};

export default function PipelineStage({ stages, currentStage, chatMode }: Props) {
  // Determine current step index
  let currentStep = -1;
  if (currentStage) {
    if (chatMode === 'RAG') {
      currentStep = ragStageMap[currentStage] ?? -1;
    } else {
      currentStep = ragStageMap[currentStage] ?? -1;
    }
  }

  // Map to agent status for coloring
  const agentStatus = currentStage ? agentStageMap[currentStage] || currentStage.toUpperCase() : 'IDLE';
  const statusColor = statusColorMap[agentStatus] || 'gray';

  return (
    <div
      className="rounded-lg px-4 py-2 flex items-center gap-4 overflow-x-auto"
      style={{
        background: `linear-gradient(90deg, #f0f9ff 0%, #e0f2fe 50%, #f0f9ff 100%)`,
        border: '1px solid #bae0fd',
      }}
    >
      <Space size={4} className="flex-shrink-0">
        <Text strong className="text-xs" style={{ color: '#0369a1' }}>
          {chatMode === 'RAG' ? 'RAG 流水线' : 'ReAct 循环'}
        </Text>
      </Space>

      <div className="flex items-center gap-1 flex-1 min-w-0">
        {stages.map((stage, idx) => {
          const isCompleted = currentStep > idx;
          const isActive = currentStep === idx;

          let dotStyle = 'w-2 h-2 rounded-full ';
          if (isCompleted) dotStyle += 'bg-blue-500';
          else if (isActive) dotStyle += 'bg-blue-500 animate-pulse';
          else dotStyle += 'bg-gray-300';

          return (
            <React.Fragment key={stage.key}>
              <div className="flex flex-col items-center gap-1 min-w-0">
                <div className="flex items-center gap-1.5">
                  <div className={dotStyle} />
                  <Text
                    className={`text-xs whitespace-nowrap ${
                      isActive
                        ? 'text-blue-600 font-bold'
                        : isCompleted
                        ? 'text-gray-700'
                        : 'text-gray-400'
                    }`}
                  >
                    {stage.icon} {stage.label}
                  </Text>
                </div>
                {isActive && (
                  <div className="w-full h-0.5 bg-blue-400 rounded animate-pulse" />
                )}
              </div>
              {idx < stages.length - 1 && (
                <div
                  className={`h-px flex-1 min-w-4 ${
                    isCompleted ? 'bg-blue-400' : 'bg-gray-200'
                  }`}
                />
              )}
            </React.Fragment>
          );
        })}
      </div>

      {/* Agent status badge */}
      {chatMode === 'Agent' && currentStage && (
        <Tag
          color={statusColor}
          style={{ color: statusColor !== 'gray' ? 'white' : undefined }}
          className="text-xs flex-shrink-0"
        >
          {agentStatus}
        </Tag>
      )}
    </div>
  );
}
