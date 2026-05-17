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

package com.sai.chat.agent.framework.distributedid;

import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.mingri.toolkit.SnowflakeIdUtil;
import org.springframework.stereotype.Component;

/**
 * 自定义 ID 生成器
 * 基于 distributedid-spring-boot-starter 的 SnowflakeIdUtil，替换 MyBatisPlus 默认的分布式 ID 生成策略
 */
@Component
public class CustomIdentifierGenerator implements IdentifierGenerator {

    @Override
    public Number nextId(Object entity) {
        return SnowflakeIdUtil.nextId();
    }

    @Override
    public String nextUUID(Object entity) {
        return SnowflakeIdUtil.nextIdStr();
    }
}
