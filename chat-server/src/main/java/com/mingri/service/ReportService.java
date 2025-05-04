package com.mingri.service;


import jakarta.servlet.http.HttpServletResponse;

public interface ReportService {
    /**
     * 导出数据报表
     * @param response
     */
    void exportClockData(HttpServletResponse response);
}
