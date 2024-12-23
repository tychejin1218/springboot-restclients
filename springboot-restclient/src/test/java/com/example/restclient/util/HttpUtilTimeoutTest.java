package com.example.restclient.util;

import static org.junit.jupiter.api.Assertions.assertThrows;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.ResourceAccessException;

@Slf4j
@SpringBootTest
class HttpUtilTimeoutTest {

  @Autowired
  HttpUtil httpUtil;

  @Test
  @DisplayName("GET 요청: 3초 연결 타임아웃")
  public void testConnectTimeout() {

    // Given: 연결 불가능한 IP 주소 또는 포트 지정 - 연결 불가능한 IP 주소
    String targetUrl = "http://127.0.0.1";

    // When & Then: 연결 타임아웃이 발생
    assertThrows(ResourceAccessException.class, () -> {
      httpUtil.sendGet(targetUrl, null, String.class);
    });
  }

  @Test
  @DisplayName("GET 요청: 5초 읽기 타임아웃")
  public void testReadTimeout() {

    // Given: 읽기 타임아웃을 유도하는 테스트 API 서버 사용 - 10초 동안 응답을 지연시킴
    String targetUrl = "http://httpbin.org/delay/10"; //

    // When & Then: 읽기 타임아웃이 발생
    assertThrows(ResourceAccessException.class, () -> {
      httpUtil.sendGet(targetUrl, null, String.class);
    });
  }
}
