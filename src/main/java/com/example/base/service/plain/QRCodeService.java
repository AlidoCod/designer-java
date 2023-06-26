package com.example.base.service.plain;

import com.example.base.constant.GenericConstant;
import com.example.base.exception.GlobalRuntimeException;
import com.example.base.util.QRCodeUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static com.example.base.service.plain.SysResourceService.CONTENT_DISPOSITION;

@Slf4j
@RequiredArgsConstructor
@Service
public class QRCodeService {

    @Value("${QR.uri}")
    String host;

    @Value("${QR.route}")
    String route;
    public void generateQRCode(HttpServletResponse response, String content) {
        try {
            /**
             * 设置传输格式
             */
            response.setContentType(GenericConstant.IMAGE_CONTENT);
            String msg = String.format("inline;filename=%d.png", System.currentTimeMillis()) ;
            response.setHeader(CONTENT_DISPOSITION, msg);
            QRCodeUtils.createCodeToOutputStream(content, response.getOutputStream());
        } catch (IOException e) {
            log.error("", e);
            throw GlobalRuntimeException.of("二维码传输异常");
        }
    }

    public void generatePayQRCode(HttpServletResponse response, Long money) {
        String content = host + route + money;
        generateQRCode(response, content);
    }
}
