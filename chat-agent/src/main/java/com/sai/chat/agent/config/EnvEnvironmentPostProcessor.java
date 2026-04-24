package com.sai.chat.agent.config;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class EnvEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String ENV_FILE_NAME = ".env";

    @Override
    @SuppressWarnings("unchecked")
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        File envFile = findEnvFile();

        Dotenv dotenv;
        if (envFile != null && envFile.exists()) {
            log.info("[EnvLoader] 加载配置文件: {}", envFile.getAbsolutePath());
            dotenv = Dotenv.configure()
                    .directory(envFile.getParentFile().getAbsolutePath())
                    .filename(envFile.getName())
                    .ignoreIfMalformed()
                    .load();
        } else {
            log.info("[EnvLoader] 未找到 .env 文件，将使用系统环境变量和默认值");
            dotenv = Dotenv.configure()
                    .ignoreIfMalformed()
                    .load();
        }

        Map<String, Object> envVars = new HashMap<>();

        dotenv.entries().forEach(entry -> {
            String key = entry.getKey();
            String value = entry.getValue();

            if (key == null || key.trim().isEmpty() || key.trim().startsWith("#")) {
                return;
            }

            envVars.put(key.trim(), value);
        });

        MapPropertySource propertySource = new MapPropertySource("dotenv", envVars);
        environment.getPropertySources().addFirst(propertySource);

        log.info("[EnvLoader] 已加载 {} 个环境变量配置项", envVars.size());
    }

    private File findEnvFile() {
        String cwd = System.getProperty("user.dir");
        File file = new File(cwd, ENV_FILE_NAME);
        if (file.exists()) {
            return file;
        }

        File current = new File(cwd);
        for (int i = 0; i < 5; i++) {
            File parent = current.getParentFile();
            if (parent == null) break;
            File envFile = new File(parent, ENV_FILE_NAME);
            if (envFile.exists()) return envFile;
            current = parent;
        }

        file = new File(System.getProperty("user.home"), ENV_FILE_NAME);
        return file.exists() ? file : null;
    }
}
