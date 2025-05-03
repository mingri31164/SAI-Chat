package com.mingri.utils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public class WebUtils {

    /**
     * @Description: 将字符串渲染到客户端
     * @Author: mingri31164
     * @Date: 2025/1/22 23:20
     **/
    public static String renderString(HttpServletResponse response, String string) {
        try
        {
            response.setStatus(200);
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            response.getWriter().print(string);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }

}
