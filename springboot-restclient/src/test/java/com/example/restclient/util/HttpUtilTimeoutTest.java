package com.example.restclient.util;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okhttp3.mockwebserver.SocketPolicy;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpHeaders;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;

@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest
class HttpUtilTimeoutTest {

  @Autowired
  HttpUtil httpUtil;

  @Autowired
  ObjectMapper objectMapper;

  MockWebServer mockWebServer;

  @BeforeEach
  void setUp() throws Exception {
    mockWebServer = new MockWebServer();
    mockWebServer.start();
  }

  @AfterEach
  void tearDown() throws Exception {
    if (mockWebServer != null) {
      TimeUnit.SECONDS.sleep(1);
      mockWebServer.shutdown();
    }
  }

  void setMockResponse(long delay) {
    mockWebServer.enqueue(new MockResponse()
        .setBody("성공")
        .addHeader(HttpHeaders.CONTENT_TYPE,
            ContentType.TEXT_PLAIN.withCharset(StandardCharsets.UTF_8).toString())
        .setResponseCode(200)
        .setHeadersDelay(delay, TimeUnit.SECONDS));
  }

  @Order(1)
  @DisplayName("연결 중 타임아웃 발생 (3초 설정)")
  @Test
  void testConnectTimeout() {

    // Given: 비정상 IP 주소로 URL 설정
    String targetUrl = "http://10.255.255.1:8080";

    // When & Then: 연결 타임아웃 예외 발생
    ResourceAccessException exception = assertThrows(ResourceAccessException.class, () -> {
      httpUtil.sendGet(targetUrl, null, String.class);
    });

    Throwable cause = exception.getCause();
    log.debug("ConnectTimeout Exception Cause: ", cause);

    assertAll(
        () -> assertNotNull(cause),
        () -> assertEquals("org.apache.hc.client5.http.ConnectTimeoutException",
            cause.getClass().getName())
    );
  }

  @Order(2)
  @DisplayName("3초 지연 후 HTTP 200 반환 (5초 설정)")
  @Test
  void testHttp200With3SecondDelay() throws Exception {

    // Given: 3초 지연 후 HTTP 200 응답
    setMockResponse(3);

    String targetUrl = mockWebServer.url("/").toString();

    // When
    ResponseEntity<String> response = httpUtil.sendGet(targetUrl, null, String.class);
    log.debug("response : {}", objectMapper.writeValueAsString(response));

    // Then
    assertAll(
        () -> assertNotNull(response),
        () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
        () -> assertNotNull(response.getBody()),
        () -> assertFalse(Objects.requireNonNull(response.getBody()).isEmpty())
    );
  }

  @Order(3)
  @DisplayName("응답 중 타임아웃 발생 (5초 설정)")
  @Test
  void testResponseTimeout() {

    // Given: 10초 지연 후 HTTP 200 응답을 반환
    setMockResponse(10);

    String targetUrl = mockWebServer.url("/").toString();

    // When & Then: 응답 타임아웃 발생 확인
    ResourceAccessException exception = assertThrows(ResourceAccessException.class, () -> {
      httpUtil.sendGet(targetUrl, null, String.class);
    });

    Throwable cause = exception.getCause();
    log.debug("ResponseTimeout Exception Cause: ", cause);

    assertAll(
        () -> assertNotNull(cause),
        () -> assertEquals("java.net.SocketTimeoutException", cause.getClass().getName())
    );
  }
}
