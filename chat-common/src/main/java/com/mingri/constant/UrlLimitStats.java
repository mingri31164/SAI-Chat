package com.mingri.constant;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UrlLimitStats {
    private int violationCount = 0;
    private boolean blocked = false;
    private LocalDateTime lastViolationTime;
}
