package com.hrbank.hrbank.global.util;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ClientIpUtilTest {

    @Test
    void 클라이언트_IP를_추출한다() {
        // 가짜 HTTP 요청 객체 생성
        HttpServletRequest request = mock(HttpServletRequest.class);
        // 요청 IP 주소를 지정
        when(request.getRemoteAddr()).thenReturn("192.168.0.10");
        // IP 추출 메서드 실행
        String clientIp = ClientIpUtil.getClientIp(request);

        assertEquals("192.168.0.10", clientIp);
    }


}
