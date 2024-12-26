# [Spring] Apache HttpClient 5 기반 RestClient 구성하기

`RestClient`는 HTTP 요청 시 다양한 HttpClient 라이브러리를 사용할 수 있으며, 이러한 라이브러리는 **`ClientHttpRequestFactory`**
인터페이스의 구현체에 의해 처리됩니다.

`RestClient`에서 사용되는 **`ClientHttpRequestFactory`** 구현체는 다음과 같습니다.

- **`JdkClientHttpRequestFactory`**: Java의 기본 HttpClient를 사용
- **`HttpComponentsClientHttpRequestFactory`**: Apache HttpClient를 사용
- **`JettyClientHttpRequestFactory`**: Jetty의 HttpClient를 사용
- **`ReactorNettyClientHttpRequestFactory`**: Reactor Netty의 HttpClient를 사용
- **`SimpleClientHttpRequestFactory`**: 기본 제공되는 간단한 구현체로, 소규모 프로젝트에 적합

이번 글에서는 Apache HttpClient 5를 사용하여 `HttpComponentsClientHttpRequestFactory`를 구성하고, 이를 활용해
`RestClient`를 설정하는 방법을 알아보겠습니다.

> **참고
**: [Spring REST Client 공식 문서](https://docs.spring.io/spring-framework/reference/integration/rest-clients.html#rest-restclient)
---

## **1. Apache HttpClient 5의 주요 기능**

Apache HttpClient 5는 다음과 같은 기능을 제공합니다.

- HTTP 표준을 준수하며, 네이티브 라이브러리에 의존하지 않고 순수 Java로 개발되었음
- HTTP/1.0, HTTP/1.1, HTTP/2.0(비동기 API 전용) 지원으로 다양한 프로토콜과 호환 가능
- HTTPS(SSL을 통한 HTTP) 암호화를 지원하여 보안성을 강화
- 네트워크 연결 방식을 자유롭게 설정할 수 있으며, TLS 암호화 연결도 쉽게 구현 가능
- HTTP/1.0 및 HTTP/1.1 프록시를 통한 데이터 중계와 `CONNECT` 메서드를 활용한 TLS 암호화 터널링 지원
- Basic(단순 유저 비번), Digest(해시 암호화), Bearer(OAuth 토큰) 인증 방식을 제공하여 다양한 인증 시나리오를 지원
- HTTP 요청/응답 간 쿠키를 자동 관리하여 세션 유지와 상태 관리를 간편하게 처리
- HTTP 연결 재사용 및 연결 풀링으로 네트워크 자원을 절약하고 요청 속도를 최적화
- HTTP 응답 데이터를 캐싱하여 반복 요청 시 서버 부하를 줄이고 빠른 응답을 제공
- 소스 코드는 Apache 라이선스를 통해 무료로 제공되며, 상업적/비상업적 프로젝트 모두에서 사용 가능

> **참고
**: [Apache HttpClient 5.2 공식 사이트](https://hc.apache.org/httpcomponents-client-5.4.x/index.html)
---

## **2. Gradle 의존성 추가**

Gradle 프로젝트에서 Apache HttpClient 5를 설정하려면 아래와 같이 의존성을 추가하세요.

```groovy
dependencies {
    // Apache HttpClient
    implementation 'org.apache.httpcomponents.client5:httpclient5:5.4.1'
}
```

> **참고
**: [Maven Repository](https://mvnrepository.com/artifact/org.apache.httpcomponents.client5/httpclient5)

---

## **3. Apache HttpClient 5 주요 메서드**

RestClient를 구성할 때 사용되는 주요 Apache HttpClient 메서드는 아래와 같습니다.

```java

@Bean
public HttpClient httpClient() {
  return HttpClients.custom()
      .setConnectionBackoffStrategy(new DefaultBackoffStrategy())
      .setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy())
      .setRetryStrategy(buildRetryStrategy())
      .setConnectionReuseStrategy(DefaultConnectionReuseStrategy.INSTANCE)
      .setDefaultRequestConfig(requestConfig())
      .setConnectionManager(buildConnectionManager())
      .evictExpiredConnections()
      .evictIdleConnections(TimeValue.ofSeconds(MAX_IDLE_TIME))
      .build();
}

```

### **주요 메서드 설명**

- **setConnectionManager(buildConnectionManager())**: HTTP 연결 풀(pool)을 효율적으로 관리하고, 잘못된 연결을 방지
- **setConnectionReuseStrategy()**: 기존 TCP 연결을 재사용하도록 설정하여 서버와 클라이언트의 비용을 절감
- **setKeepAliveStrategy()**: HTTP Keep-Alive 설정을 통해 유지 및 재사용 가능한 커넥션을 만들어 성능을 최적화
- **setRetryStrategy(buildRetryStrategy())**: 트워크 장애나 특정 오류 상태에 따라 재시도 전략을 정의
- **setConnectionBackoffStrategy()**: 네트워크 혼잡이나 장애 발생 시 요청 지연 또는 재요청의 동작을 제어
- **setDefaultRequestConfig(requestConfig())**: 기본 HTTP 요청 설정(예: 타임아웃, 연결 설정)을 지정
- **evictExpiredConnections()**: 이미 만료된 HTTP 연결을 정리하여 유효하지 않은 연결이 재사용되는 것을 방지
- **evictIdleConnections(TimeValue.ofSeconds())**: 설정된 시간 동안 사용되지 않은 유휴 연결을 제거하여 리소스 낭비를 방지

> **참고
**: [Apache HttpClient의 공식 JavaDoc](https://hc.apache.org/httpcomponents-client-5.2.x/current/apidocs/)
---

## **4. RestClientConfig 설정**

Apache HttpClient 5를 기반으로 `RestClient`를 구성해 보겠습니다.

### **RestClientConfig 클래스**
```java
package com.example.restclient.config;

import java.util.concurrent.TimeUnit;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.DefaultConnectionKeepAliveStrategy;
import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy;
import org.apache.hc.client5.http.impl.classic.DefaultBackoffStrategy;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.http.impl.DefaultConnectionReuseStrategy;
import org.apache.hc.core5.util.TimeValue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

  // 연결 풀(Connection Pool) 설정 값
  private static final int MAX_TOTAL_CONNECTIONS = 200; // 최대 전체 커넥션 수
  private static final int MAX_CONNECTIONS_PER_ROUTE = 20; // 특정 호스트(경로)별 최대 커넥션 수
  private static final int MAX_IDLE_TIME = 30; // 유휴 연결 유지 시간 (초 단위)

  // 재시도 설정 값
  private static final int MAX_RETRIES = 1; // 요청 실패 시 재시도 횟수
  private static final long RETRY_INTERVAL_IN_SECONDS = 1L; // 재시도 간격 (초 단위)

  // 타임아웃 설정 값
  private static final long RESPONSE_TIMEOUT = 5L; // 응답 타임아웃 (초 단위)
  private static final long CONNECTION_REQUEST_TIMEOUT = 3L; // 연결 요청 타임아웃 (초 단위)

  /**
   * RestClient 빈을 생성
   *
   * @param httpClient 설정된 HttpClient 객체
   * @return RestClient 객체
   */
  @Bean
  public RestClient restClient(HttpClient httpClient) {
    return RestClient.builder()
        .requestFactory(new HttpComponentsClientHttpRequestFactory(httpClient))
        .build();
  }

  /**
   * HttpClient 빈을 생성
   *
   * @return HttpClient 객체
   */
  @Bean
  public HttpClient httpClient() {
    return HttpClients.custom()
        .setConnectionManager(buildConnectionManager())
        .setConnectionReuseStrategy(DefaultConnectionReuseStrategy.INSTANCE)
        .setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy())
        .setRetryStrategy(buildRetryStrategy())
        .setConnectionBackoffStrategy(new DefaultBackoffStrategy())
        .setDefaultRequestConfig(requestConfig())
        .evictExpiredConnections()
        .evictIdleConnections(TimeValue.ofSeconds(MAX_IDLE_TIME))
        .build();
  }

  /**
   * 연결 풀(Connection Pool)을 생성하고 최대 연결 수와 라우트별 최대 연결 수를 설정
   *
   * @return PoolingHttpClientConnectionManager 객체
   */
  private PoolingHttpClientConnectionManager buildConnectionManager() {
    PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
    connectionManager.setMaxTotal(MAX_TOTAL_CONNECTIONS);
    connectionManager.setDefaultMaxPerRoute(MAX_CONNECTIONS_PER_ROUTE);
    return connectionManager;
  }

  /**
   * HTTP 요청의 응답 및 연결 요청 시간을 설정
   *
   * @return RequestConfig 객체
   */
  private RequestConfig requestConfig() {
    return RequestConfig.custom()
        .setResponseTimeout(RESPONSE_TIMEOUT, TimeUnit.SECONDS)
        .setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT, TimeUnit.SECONDS)
        .build();
  }

  /**
   * HTTP 요청 실패 시 재시도 전략을 설정
   *
   * @return DefaultHttpRequestRetryStrategy 객체
   */
  private DefaultHttpRequestRetryStrategy buildRetryStrategy() {
    return new DefaultHttpRequestRetryStrategy(
        MAX_RETRIES, TimeValue.ofSeconds(RETRY_INTERVAL_IN_SECONDS)
    );
  }
}

```

---

## **5. RestClient 유틸리티 클래스**

`RestClient`를 사용하여 유틸리티 클래스를 작성하겠습니다.

### **HttpUtil 클래스**
```java
package com.example.restclient.util;

import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

/**
 * RestClient를 활용한 HTTP 요청(GET, POST, PUT, DELETE)을 위한 유틸리티 클래스
 */

@AllArgsConstructor
@Component
public class HttpUtil {

  private final RestClient restClient;

  /**
   * GET 요청을 보내고 응답을 객체로 반환
   *
   * @param targetUrl    요청을 보낼 URL
   * @param headers      요청 헤더 정보
   * @param responseType 응답을 매핑할 클래스 타입
   * @return 응답 객체
   */
  public <T> ResponseEntity<T> sendGet(String targetUrl, MultiValueMap<String, String> headers,
      Class<T> responseType) {
    return restClient.get()
        .uri(targetUrl)
        .accept(MediaType.APPLICATION_JSON)
        .headers(httpHeaders -> {
          if (headers != null && !headers.isEmpty()) {
            httpHeaders.addAll(headers);
          }
        })
        .retrieve()
        .toEntity(responseType);
  }

  /**
   * POST 요청을 보내고 응답을 객체로 반환
   *
   * @param targetUrl    요청을 보낼 URL
   * @param headers      요청 헤더 정보
   * @param body         요청 본문 객체
   * @param responseType 응답을 매핑할 클래스 타입
   * @return 응답 객체
   */
  public <T> ResponseEntity<T> sendPost(String targetUrl, MultiValueMap<String, String> headers,
      Object body, Class<T> responseType) {
    return restClient.post()
        .uri(targetUrl)
        .contentType(MediaType.APPLICATION_JSON)
        .headers(httpHeaders -> {
          if (headers != null && !headers.isEmpty()) {
            httpHeaders.addAll(headers);
          }
        })
        .body(body)
        .retrieve()
        .toEntity(responseType);
  }

  /**
   * PUT 요청을 보내고 응답을 객체로 반환
   *
   * @param targetUrl    요청을 보낼 URL
   * @param headers      요청 헤더 정보
   * @param body         요청 본문 객체
   * @param responseType 응답을 매핑할 클래스 타입
   * @return 응답 객체
   */
  public <T> ResponseEntity<T> sendPut(String targetUrl, MultiValueMap<String, String> headers,
      Object body, Class<T> responseType) {
    return restClient.put()
        .uri(targetUrl)
        .contentType(MediaType.APPLICATION_JSON)
        .headers(httpHeaders -> {
          if (headers != null && !headers.isEmpty()) {
            httpHeaders.addAll(headers);
          }
        })
        .body(body)
        .retrieve()
        .toEntity(responseType);
  }

  /**
   * DELETE 요청을 보내고 응답을 객체로 반환
   *
   * @param targetUrl    요청을 보낼 URL
   * @param headers      요청 헤더 정보
   * @param responseType 응답을 매핑할 클래스 타입
   * @return 응답 객체
   */
  public <T> ResponseEntity<T> sendDelete(String targetUrl, MultiValueMap<String, String> headers,
      Class<T> responseType) {
    return restClient.delete()
        .uri(targetUrl)
        .headers(httpHeaders -> {
          if (headers != null && !headers.isEmpty()) {
            httpHeaders.addAll(headers);
          }
        })
        .retrieve()
        .toEntity(responseType);
  }
}

```

---

## **6. 단위 테스트**

`RestClient`를 활용해 만든 유틸리티 클래스인 `HttpUtil`의 동작을 검증하기 위한 단위 테스트를 확인해보겠습니다.

### **HttpUtilTest 클래스**
```java
package com.example.restclient.util;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest
class HttpUtilTest {

  @Autowired
  HttpUtil httpUtil;

  @Autowired
  ObjectMapper objectMapper;

  private final String TEST_GET_URL = "https://jsonplaceholder.typicode.com/posts/1";
  private final String TEST_POST_URL = "https://jsonplaceholder.typicode.com/posts";
  private final String TEST_PUT_URL = "https://jsonplaceholder.typicode.com/posts/1";
  private final String TEST_DELETE_URL = "https://jsonplaceholder.typicode.com/posts/1";

  @Order(1)
  @DisplayName("GET 요청: ID를 기준으로 포스트 조회 후 응답 ID 확인")
  @Test
  public void testGetRequest() throws Exception {

    // Given
    MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
    headers.add("key1", "value1");
    headers.add("key1", "value2");
    headers.add("key2", "value1");

    // When
    ResponseEntity<PostDTO> response = httpUtil.sendGet(TEST_GET_URL, headers, PostDTO.class);
    log.debug("response: {}", objectMapper.writeValueAsString(response.getBody()));

    // Then
    assertAll(
        () -> assertNotNull(response),
        () -> assertNotNull(response.getBody()),
        () -> assertEquals(1, response.getBody().getId())
    );
  }

  @Order(2)
  @DisplayName("POST 요청: 포스트 저장 후 응답의 title과 body 확인")
  @Test
  public void testPostRequest() throws Exception {

    // Given
    MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
    headers.add("key1", "value1");
    headers.add("key1", "value2");
    headers.add("key2", "value1");

    PostDTO postData = PostDTO.builder()
        .title("foo")
        .body("bar")
        .userId(1)
        .build();

    // When
    ResponseEntity<PostDTO> response = httpUtil.sendPost(TEST_POST_URL, headers,
        postData, PostDTO.class);
    log.debug("response: {}", objectMapper.writeValueAsString(response.getBody()));

    // Then
    assertAll(
        () -> assertNotNull(response),
        () -> assertNotNull(response.getBody()),
        () -> assertEquals("foo", response.getBody().getTitle()),
        () -> assertEquals("bar", response.getBody().getBody())
    );
  }

  @Order(3)
  @DisplayName("PUT 요청: 포스트 수정 후 응답의 title과 body 확인")
  @Test
  public void testPutRequest() throws Exception {

    // Given
    MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
    headers.add("key1", "value1");
    headers.add("key1", "value2");
    headers.add("key2", "value1");

    PostDTO putData = PostDTO.builder()
        .id(1)
        .title("foo")
        .body("bar")
        .userId(1)
        .build();

    // When
    ResponseEntity<PostDTO> response = httpUtil.sendPut(TEST_PUT_URL, headers,
        putData, PostDTO.class);
    log.debug("response: {}", objectMapper.writeValueAsString(response.getBody()));

    // Then
    assertAll(
        () -> assertNotNull(response),
        () -> assertNotNull(response.getBody()),
        () -> assertEquals("foo", response.getBody().getTitle()),
        () -> assertEquals("bar", response.getBody().getBody())
    );
  }

  @Order(4)
  @DisplayName("DELETE 요청: 포스트 삭제 후 응답이 빈 값인지 확인")
  @Test
  public void testDeleteRequest() throws Exception {

    // Given
    MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
    headers.add("key1", "value1");
    headers.add("key1", "value2");
    headers.add("key2", "value1");

    // When
    ResponseEntity<PostDTO> response = httpUtil.sendDelete(TEST_DELETE_URL, headers, PostDTO.class);
    log.debug("response: {}", objectMapper.writeValueAsString(response.getBody()));

    // Then
    assertAll(
        () -> assertNotNull(response),
        () -> assertNotNull(response.getBody()),
        () -> assertEquals(null, response.getBody().getTitle()),
        () -> assertEquals(null, response.getBody().getBody())
    );
  }

  @Getter
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class PostDTO {

    private int id;
    private String title;
    private String body;
    private int userId;
  }
}

```

---
