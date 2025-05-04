package com.mingri.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;


/**
 *配置启动链接接口地址
 */
@Component
@Slf4j
public class DocumentationConfig  implements ApplicationListener<ApplicationReadyEvent> {

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        Environment env = event.getApplicationContext().getEnvironment();

        String protocol = "http";
        if (env.getProperty("server.ssl.key-store") != null) {
            protocol = "https";
        }
        String serverPort = env.getProperty("server.port");
        String contextPath = env.getProperty("server.servlet.context-path", "");
        String docPath = contextPath + "/doc.html";

        String hostAddress = "localhost";
        try {
            hostAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            log.warn("The host name could not be determined, using `localhost` as fallback");
        }

        log.info("""
                        \t
                        ----------------------------------------------------------\t
                        应用程序“ {} ”正在运行中......\t
                        配置文件: \t{}
                        ///////////////////////////////////////////////////
                        //  く__,.ヘヽ.        /  ,ー､ 〉                 //
                        //           ＼ ', !-─‐-i  /  /´                 //
                        //           ／｀ｰ'       L/／｀ヽ､               //
                        //         /   ／,   /|   ,   ,       ',         //
                        //       ｲ   / /-‐/  ｉ  L_ ﾊ ヽ!   i            //
                        //        ﾚ ﾍ 7ｲ｀ﾄ   ﾚ'ｧ-ﾄ､!ハ|   |              //
                        //          !,/7 '0'     ´0iソ|    |             //
                        //          |.从"    _     ,,,, / |./    |       //
                        //          ﾚ'| i＞.､,,__  _,.イ /   .i   |      //
                        //            ﾚ'| | / k_７_/ﾚ'ヽ,  ﾊ.  |         //
                        //              | |/i 〈|/   i  ,.ﾍ |  i  |      //
                        //             .|/ /  ｉ：    ﾍ!    ＼  |        //
                        //              kヽ>､ﾊ    _,.ﾍ､    /､!           //
                        //              !'〈//｀Ｔ´', ＼ ｀'7'ｰr'        //
                        //              ﾚ'ヽL__|___i,___,ンﾚ|ノ          //
                        //                  ﾄ-,/  |___./                //
                        //                  'ｰ'    !_,.:                //
                        //      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^      //
                        //             面试必过      早进大厂             //
                        //////////////////////////////////////////////////\t
                        接口文档访问 URL:\t
                        本地: \t{}://localhost:{}{}\t
                        外部: \t{}://{}:{}{}
                        ----------------------------------------------------------""",
                env.getProperty("spring.application.name"),
                String.join(",", env.getActiveProfiles()),
                protocol,
                serverPort,
                docPath,
                protocol,
                hostAddress,
                serverPort,
                docPath
                );
    }
}
