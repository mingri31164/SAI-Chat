package com.mingri.controller.admin;

import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 数据统计相关接口
 */
@RestController
@RequestMapping("/api/v1/admin/report")
@Api(tags = "数据统计相关接口")
@Slf4j
public class ReportController {

//    @Autowired
//    private ReportService reportService;
//
//    /**
//     * 导出数据报表
//     * @param response
//     */
//    @GetMapping("/export")
//    @ApiOperation("导出数据报表")
//    public void export(HttpServletResponse response){
//        reportService.exportClockData(response);
//    }

}
