package com.mingri.constant.type;

import lombok.Data;

@Data
public class WsContentType {
    //消息
    public static final String Msg = "msg";
    //通知
    public static final String Notify = "notify";
    //视频/音频
    public static String Video = "video";
    //文件
    public static String File = "file";
}
