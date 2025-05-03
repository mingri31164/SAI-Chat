package com.mingri.utils;

import org.lionsoul.ip2region.xdb.Searcher;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class IpUtil {

    private static final String LOCAL_IP = "127.0.0.1";
    private static Searcher searcher;

    public static String getIpAddr(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Forwarded-For");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }

        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        return "0:0:0:0:0:0:0:1".equals(ip) ? LOCAL_IP : ip;
    }


    /**
     * 判断是否为合法 IP
     *
     * @return
     */
    public static boolean checkIp(String ipAddress) {
        String ip = "([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}";
        Pattern pattern = Pattern.compile(ip);
        Matcher matcher = pattern.matcher(ipAddress);
        return matcher.matches();
    }

    /**
     * 加载ip2region
     */
    @PostConstruct
    private static void initIp2Region() {
        try {
            InputStream inputStream = new ClassPathResource("/ip2region.xdb").getInputStream();
            byte[] bytes = FileCopyUtils.copyToByteArray(inputStream);
            searcher = Searcher.newWithBuffer(bytes);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    /**
     * 获取 ip 所属地址
     *
     * @param ip ip
     * @return
     */
    public static String getIpRegion(String ip) {
        boolean isIp = checkIp(ip);
        if (isIp) {
            initIp2Region();
            try {
                String searchIpInfo = searcher.search(ip);
                String[] splitIpInfo = searchIpInfo.split("\\|");
                if (splitIpInfo.length > 0) {
                    if ("中国".equals(splitIpInfo[0])) {
                        return splitIpInfo[2];
                    } else if ("0".equals(splitIpInfo[0])) {
                        if ("内网IP".equals(splitIpInfo[4])) {
                            return "内网";
                        } else {
                            return "未知";
                        }
                    } else {
                        if ("0".equals(splitIpInfo[0])) {
                            return "未知";
                        }
                        return splitIpInfo[0];
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return "未知";
        } else {
            return ip;
        }

    }
}
