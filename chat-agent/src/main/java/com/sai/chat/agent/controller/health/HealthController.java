package com.sai.chat.agent.controller.health;

import com.sai.chat.agent.framework.convention.Result;
import com.sai.chat.agent.framework.web.Results;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/health")
    public Result<String> health() {
        return Results.success("OK");
    }
}
