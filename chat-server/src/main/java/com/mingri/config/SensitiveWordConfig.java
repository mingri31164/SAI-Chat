package com.mingri.config;

import com.github.houbb.sensitive.word.bs.SensitiveWordBs;
import com.github.houbb.sensitive.word.support.ignore.SensitiveWordCharIgnores;
import com.github.houbb.sensitive.word.support.resultcondition.WordResultConditions;
import com.github.houbb.sensitive.word.support.tag.WordTags;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SensitiveWordConfig {

    @Bean
    public SensitiveWordBs sensitiveWordBs() {
        SensitiveWordBs wordBs = SensitiveWordBs.newInstance()
                .ignoreCase(true)
                .ignoreWidth(true)
                .ignoreNumStyle(true)
                .ignoreChineseStyle(true)
                .ignoreEnglishStyle(true)
                .ignoreRepeat(false)
                .enableNumCheck(false)
                .enableEmailCheck(false)
                .enableUrlCheck(false)
                .enableIpv4Check(false)
                .enableWordCheck(true)
                .numCheckLen(8)
                .wordTag(WordTags.none())
                .charIgnore(SensitiveWordCharIgnores.defaults())
                .wordResultCondition(WordResultConditions.alwaysTrue())
                .init();
        return wordBs;
    }

}
