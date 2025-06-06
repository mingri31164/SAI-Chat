package com.mingri.core.sensitive;

import cn.hutool.core.util.BooleanUtil;
import com.github.houbb.sensitive.word.bs.SensitiveWordBs;
import com.github.houbb.sensitive.word.core.SensitiveWordHelper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 敏感词替换服务
 * @author: qing
 * @Date: 2024/7/29
 */
@Service
public class SensitiveService {

    @Autowired
    private SensitiveProperties sensitiveProperties;

    private volatile SensitiveWordBs sensitiveWordBs;

    @PostConstruct
    public void init() {
        sensitiveWordBs = SensitiveWordBs.newInstance().init();
    }

    /**
     * 是否包含敏感词
     * @param str
     * @return
     */
    public boolean contains(String str) {
        if (BooleanUtil.isTrue(sensitiveProperties.isEnabled())) {
            return SensitiveWordHelper.contains(str);
        }
        return false;
    }

    /**
     * 敏感词替换
     * @param str
     * @return
     */
    public String replace(String str) {
        if (BooleanUtil.isTrue(sensitiveProperties.isEnabled())) {
            return SensitiveWordHelper.replace(str);
        }
        return str;
    }

    public List<String> findAll(String str) {
        return SensitiveWordHelper.findAll(str);
    }

    /**
     * 全局静态敏感词替换（不依赖Spring注入，适合工具类调用）
     */
    public static String staticReplace(String str) {
        // 这里直接用SensitiveWordHelper，配置开关可根据需要调整
        return SensitiveWordHelper.replace(str);
    }
}
