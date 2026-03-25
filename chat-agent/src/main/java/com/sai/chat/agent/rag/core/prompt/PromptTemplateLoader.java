/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sai.chat.agent.rag.core.prompt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Prompt 模板加载器
 * <p>
 * 从 classpath 资源文件（如 .st / .ftl / .txt）加载 Prompt 模板
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PromptTemplateLoader {

    private final ResourceLoader resourceLoader;

    @Value("${rag.prompt.template-path:classpath:prompt}")
    private String templateBasePath;

    /**
     * 加载指定路径的模板文件
     *
     * @param templatePath classpath 相对路径，例如 "intent-classifier.st"
     * @return 模板内容字符串
     */
    public String loadTemplate(String templatePath) {
        try {
            Resource resource = resourceLoader.getResource(templateBasePath + "/" + templatePath);
            InputStream inputStream = resource.getInputStream();
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.warn("无法加载 Prompt 模板: {}, 将使用备用逻辑", templatePath, e);
            return null;
        }
    }

    /**
     * 加载并填充变量后的模板
     *
     * @param templatePath 模板相对路径
     * @param variables    变量 map
     * @return 填充后的模板内容
     */
    public String loadAndRender(String templatePath, Map<String, Object> variables) {
        String template = loadTemplate(templatePath);
        if (template == null) {
            return null;
        }
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            template = template.replace("{{" + entry.getKey() + "}}", String.valueOf(entry.getValue()));
        }
        return template;
    }
}
