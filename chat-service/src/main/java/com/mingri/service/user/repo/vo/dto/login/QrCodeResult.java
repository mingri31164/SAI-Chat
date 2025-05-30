package com.mingri.service.user.repo.vo.dto.login;

import cn.hutool.json.JSONObject;
import lombok.Data;

@Data
public class QrCodeResult {
    private String action;
    private String ip;
    private String status;
    private JSONObject extend;
}
