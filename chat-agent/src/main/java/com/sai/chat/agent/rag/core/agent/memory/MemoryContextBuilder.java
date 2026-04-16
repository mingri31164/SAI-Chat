/*
 * 记忆上下文构建器
 * 
 * 负责整合多层记忆，生成完整的上下文给LLM
 */

package com.sai.chat.agent.rag.core.agent.memory;

import com.sai.chat.agent.rag.core.agent.memory.MultiLevelMemoryManager.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 记忆上下文构建器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MemoryContextBuilder {

    /**
     * 构建完整上下文
     */
    public String buildContext(WorkingMemory working, 
                               EpisodicMemory episodic,
                               SemanticMemory semantic,
                               String currentQuery) {
        StringBuilder context = new StringBuilder();
        
        // 1. 语义记忆 - 相关的持久化知识
        context.append(buildSemanticContext(semantic, currentQuery));
        
        // 2. 情景记忆 - 相关历史经验
        context.append(buildEpisodicContext(episodic, currentQuery));
        
        // 3. 工作记忆 - 当前会话上下文
        context.append(buildWorkingContext(working));
        
        return context.toString();
    }
    
    /**
     * 构建工作记忆上下文
     */
    public String buildWorkingContext(WorkingMemory working) {
        if (working == null) return "";
        
        StringBuilder sb = new StringBuilder();
        sb.append("【当前会话上下文】\n");
        
        // 当前任务
        if (working.getCurrentTask() != null) {
            sb.append("当前任务: ").append(working.getCurrentTask()).append("\n");
        }
        
        // 当前计划
        if (working.getCurrentPlan() != null) {
            sb.append("执行计划: ").append(working.getCurrentPlan()).append("\n");
        }
        
        // 最近交互
        List<MemoryEntry> recentEntries = working.getRecentEntries(5);
        if (!recentEntries.isEmpty()) {
            sb.append("最近交互:\n");
            for (MemoryEntry entry : recentEntries) {
                String typeLabel = getTypeLabel(entry.getType());
                sb.append("  - [").append(typeLabel).append("] ");
                sb.append(truncate(entry.getContent(), 100));
                sb.append("\n");
            }
        }
        
        sb.append("\n");
        return sb.toString();
    }
    
    /**
     * 构建情景记忆上下文
     */
    public String buildEpisodicContext(EpisodicMemory episodic, String query) {
        if (episodic == null || episodic.getEpisodes().isEmpty()) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("【历史经验参考】\n");
        
        // 查找相关的历史情节
        List<Episode> relevantEpisodes = episodic.getEpisodes().stream()
                .filter(e -> isRelevant(e, query))
                .collect(Collectors.toList());
        
        if (relevantEpisodes.isEmpty()) {
            // 如果没有直接相关的，显示最近的经验
            relevantEpisodes = episodic.getEpisodes().stream()
                    .skip(Math.max(0, episodic.getEpisodes().size() - 3))
                    .collect(Collectors.toList());
        }
        
        // 添加成功的经验
        List<Episode> successEpisodes = relevantEpisodes.stream()
                .filter(Episode::isSuccess)
                .limit(3)
                .collect(Collectors.toList());
        
        if (!successEpisodes.isEmpty()) {
            sb.append("成功的处理方式:\n");
            for (Episode episode : successEpisodes) {
                sb.append("  - ").append(episode.toSummary()).append("\n");
            }
        }
        
        // 添加失败的经验教训
        List<Episode> failEpisodes = relevantEpisodes.stream()
                .filter(e -> !e.isSuccess())
                .limit(2)
                .collect(Collectors.toList());
        
        if (!failEpisodes.isEmpty()) {
            sb.append("需要避免的问题:\n");
            for (Episode episode : failEpisodes) {
                sb.append("  - ").append(episode.toSummary()).append("\n");
            }
        }
        
        sb.append("\n");
        return sb.toString();
    }
    
    /**
     * 构建语义记忆上下文
     */
    public String buildSemanticContext(SemanticMemory semantic, String query) {
        if (semantic == null || semantic.size() == 0) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        
        // 搜索相关知识
        List<KnowledgeEntry> relevantKnowledge = semantic.search(query);
        
        if (!relevantKnowledge.isEmpty()) {
            sb.append("【相关知识】\n");
            
            // 按类型分组显示
            List<KnowledgeEntry> patterns = relevantKnowledge.stream()
                    .filter(k -> k.getType() == KnowledgeType.PATTERN)
                    .limit(2)
                    .collect(Collectors.toList());
            
            List<KnowledgeEntry> rules = relevantKnowledge.stream()
                    .filter(k -> k.getType() == KnowledgeType.RULE)
                    .limit(2)
                    .collect(Collectors.toList());
            
            List<KnowledgeEntry> bestPractices = relevantKnowledge.stream()
                    .filter(k -> k.getType() == KnowledgeType.BEST_PRACTICE)
                    .limit(2)
                    .collect(Collectors.toList());
            
            if (!patterns.isEmpty()) {
                sb.append("已知模式:\n");
                for (KnowledgeEntry entry : patterns) {
                    sb.append("  - ").append(entry.getKnowledge()).append("\n");
                }
            }
            
            if (!rules.isEmpty()) {
                sb.append("规则:\n");
                for (KnowledgeEntry entry : rules) {
                    sb.append("  - ").append(entry.getKnowledge()).append("\n");
                }
            }
            
            if (!bestPractices.isEmpty()) {
                sb.append("最佳实践:\n");
                for (KnowledgeEntry entry : bestPractices) {
                    sb.append("  - ").append(entry.getKnowledge()).append("\n");
                }
            }
            
            sb.append("\n");
        }
        
        return sb.toString();
    }
    
    /**
     * 构建反思总结
     */
    public String buildReflectionSummary(WorkingMemory working, 
                                        EpisodicMemory episodic,
                                        boolean success) {
        StringBuilder sb = new StringBuilder();
        sb.append("【反思总结】\n");
        
        if (success) {
            sb.append("本次任务成功完成。\n");
            
            // 提取成功的关键因素
            if (working != null) {
                List<MemoryEntry> successEntries = working.getEntriesByType(MemoryType.SUCCESS);
                if (!successEntries.isEmpty()) {
                    sb.append("成功因素:\n");
                    for (MemoryEntry entry : successEntries) {
                        sb.append("  - ").append(entry.getContent()).append("\n");
                    }
                }
            }
        } else {
            sb.append("本次任务遇到困难。\n");
            
            // 提取错误信息
            if (working != null) {
                List<MemoryEntry> errorEntries = working.getEntriesByType(MemoryType.ERROR);
                if (!errorEntries.isEmpty()) {
                    sb.append("问题分析:\n");
                    for (MemoryEntry entry : errorEntries) {
                        sb.append("  - ").append(entry.getContent()).append("\n");
                    }
                }
            }
            
            // 反思内容
            if (working != null) {
                List<MemoryEntry> reflectionEntries = working.getEntriesByType(MemoryType.REFLECTION);
                if (!reflectionEntries.isEmpty()) {
                    sb.append("改进方向:\n");
                    for (MemoryEntry entry : reflectionEntries) {
                        sb.append("  - ").append(entry.getContent()).append("\n");
                    }
                }
            }
        }
        
        // 添加历史教训
        if (episodic != null) {
            sb.append("\n").append(episodic.getLessonLearned());
        }
        
        return sb.toString();
    }
    
    private boolean isRelevant(Episode episode, String query) {
        if (episode == null || query == null) return false;
        String lowerQuery = query.toLowerCase();
        return episode.getTask().toLowerCase().contains(lowerQuery)
                || episode.getAction().toLowerCase().contains(lowerQuery);
    }
    
    private String getTypeLabel(MemoryType type) {
        if (type == null) return "未知";
        switch (type) {
            case USER_MESSAGE: return "用户";
            case AGENT_RESPONSE: return "助手";
            case TOOL_RESULT: return "工具";
            case THINKING: return "思考";
            case PLAN: return "计划";
            case REFLECTION: return "反思";
            case ERROR: return "错误";
            case SUCCESS: return "成功";
            default: return type.name();
        }
    }
    
    private String truncate(String str, int maxLen) {
        if (str == null) return "";
        if (str.length() <= maxLen) return str;
        return str.substring(0, maxLen) + "...";
    }
}
