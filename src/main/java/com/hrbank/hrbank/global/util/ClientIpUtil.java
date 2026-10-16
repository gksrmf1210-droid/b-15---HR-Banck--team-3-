package com.hrbank.hrbank.global.util;

import jakarta.servlet.http.HttpServletRequest;

public class ClientIpUtil {



    private ClientIpUtil() {

    }

    // HTTP 요청에 클라이언트 IP 주소 추출
    public static String getClientIp(HttpServletRequest request) {

        return request.getRemoteAddr();

    }


}
