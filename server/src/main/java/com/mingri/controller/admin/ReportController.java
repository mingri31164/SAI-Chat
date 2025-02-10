package com.mingri.controller.admin;

import com.mingri.service.ReportService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletResponse;

/**
 * 数据统计相关接口
 */
@RestController
@RequestMapping("/api/v1/admin/report")
@Api(tags = "数据统计相关接口")
@Slf4j
public class ReportController {

    @Autowired
    private ReportService reportService;

    /**
     * 导出数据报表
     * @param response
     */
    @GetMapping("/export")
    @ApiOperation("导出数据报表")
    public void export(HttpServletResponse response){
        reportService.exportClockData(response);
    }

}
