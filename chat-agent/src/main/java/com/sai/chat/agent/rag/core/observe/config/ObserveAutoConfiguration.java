/*
 * 观测能力自动配置
 */

package com.sai.chat.agent.rag.core.observe.config;

import com.sai.chat.agent.rag.core.observe.eval.*;
import com.sai.chat.agent.rag.core.observe.metrics.*;
import com.sai.chat.agent.rag.core.observe.trace.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 观测能力自动配置
 */
@Configuration
@EnableConfigurationProperties(ObserveProperties.class)
public class ObserveAutoConfiguration {

    // ==================== 追踪系统 ====================

    @Bean
    @ConditionalOnMissingBean
    public TraceRecorder traceRecorder() {
        return new TraceRecorder();
    }

    @Bean
    @ConditionalOnMissingBean
    public TraceQueryService traceQueryService(TraceRecorder traceRecorder) {
        return new TraceQueryService(traceRecorder);
    }

    // ==================== 指标系统 ====================

    @Bean
    @ConditionalOnMissingBean
    public MetricsCollector metricsCollector() {
        return new MetricsCollector();
    }

    @Bean
    @ConditionalOnMissingBean
    public MetricsConsoleExporter metricsConsoleExporter(MetricsCollector metricsCollector) {
        return new MetricsConsoleExporter(metricsCollector);
    }

    @Bean
    @ConditionalOnMissingBean
    public MetricsInterceptor metricsInterceptor(MetricsCollector metricsCollector,
                                                 ObserveProperties observeProperties) {
        return new MetricsInterceptor(metricsCollector, observeProperties);
    }

    // ==================== 评估系统 ====================

    @Bean
    @ConditionalOnMissingBean
    public Evaluator keywordEvaluator() {
        return new KeywordEvaluator();
    }

    @Bean
    @ConditionalOnMissingBean
    public Evaluator patternEvaluator() {
        return new PatternEvaluator();
    }

    @Bean
    @ConditionalOnMissingBean
    public Evaluator rejectEvaluator() {
        return new RejectEvaluator();
    }

    @Bean
    @ConditionalOnMissingBean
    public Evaluator toolCallEvaluator() {
        return new ToolCallEvaluator();
    }

    @Bean
    @ConditionalOnMissingBean
    public Evaluator performanceEvaluator() {
        return new PerformanceEvaluator();
    }

    @Bean
    @ConditionalOnMissingBean
    public DefaultEvalCaseLibrary defaultEvalCaseLibrary() {
        return new DefaultEvalCaseLibrary();
    }

    @Bean
    @ConditionalOnMissingBean
    public EvalRunner evalRunner(com.sai.chat.agent.rag.core.agent.AgentService agentService,
                                  List<Evaluator> evaluators) {
        return new EvalRunner(agentService, evaluators);
    }
}
