package com.example.base.constant;

import com.example.base.utils.LocalIPUtil;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.SocketException;

@Slf4j
@Component
public class GenericConstant {

    public static final String SYSTEM_NOTICE = "系统通知";

    public static final String NOT_EXAMINE = SYSTEM_NOTICE + ": 审核不通过";

    public static final String POST_FORM_SUCCESS = SYSTEM_NOTICE + ": 表单提交成功，等待支付";

    public static final String POST_FORM_SUCCESS_MESSAGE = "你好, 表单提交成功。请扫码支付，二维码的URL为: ";

    public static final String COOPERATION_REQUEST = SYSTEM_NOTICE + ": 新的合作请求";

    public static final String DESIGNER_COOPERATE_NOTICE_FORMAT = "尊敬的设计师，你好！您有一个新的合作请求。\n" +
            "以下是相关信息的URL，请仔细查看：\n" +
            "用户：%s\n" +
            "需求：%s\n" +
            "金额：%s\n" +
            "截止日期：%s";

    public static final String PAY_SUCCESS_FORMAT = "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><title>支付成功</title></head><body><h1>支付成功</h1><p>谢谢你的付款。您的交易已经完成，您的需求将很快得到处理。</p><p>支付的金额为: %s</p><p>如果您有任何问题或疑虑，请通过[8848-钛合金手机]与我们联系.</p></body></html>";

    public static final String PAY_SUCCESS = SYSTEM_NOTICE + ": 支付成功，等待确认";

    public static final String IMAGE_CONTENT = "image/png";

    public static final String ENCRYPTION_PROTOCOL = "http://";

    public static String HOST = "127.0.0.1";

    public static final String QRCODE_FORMAT = "/QRCode?id=%d&money=%s";

    public static final int FAVOUR_INIT_SCORE = 5;

    public static final int FAVOUR_DECREASE_STEP = -2;
    @PostConstruct
    public void init() {
        try {
            HOST = LocalIPUtil.getLocalIp4Address().orElseThrow(RuntimeException::new).toString().replaceAll("/", "");
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        log.debug("局域网IP为: {}", HOST);
    }

}
