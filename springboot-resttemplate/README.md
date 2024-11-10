# Spring RestTemplate를 활용한 HTTP 요청

## RestTemplate란?

`RestTemplate`은 Spring Framework의 HTTP 클라이언트로, RESTful 웹 서비스와 상호작용하여 쉽게 HTTP 요청을 보낼 수 있도록 도와줍니다.

## 주요 클래스와 메서드

- `RestTemplate`: HTTP 클라이언트를 생성하고 설정합니다.
  - `getForObject()`: GET 요청을 URL에 보내고, 결과를 객체로 반환합니다.
  - `getForEntity()`: GET 요청을 URL에 보내고, 결과를 `ResponseEntity`로 반환합니다.
  - `postForObject()`: POST 요청을 URL에 보내고, 결과를 객체로 반환합니다.
  - `postForEntity()`: POST 요청을 URL에 보내고, 결과를 `ResponseEntity`로 반환합니다.
  - `put()`: PUT 요청을 URL에 보냅니다.
  - `delete()`: DELETE 요청을 URL에 보냅니다.
  - `exchange()`: 특정 HTTP 메서드 요청을 URL에 보냅니다.
  - `headForHeaders()`: HEAD 요청을 URL에 보내고, 결과로 헤더 정보를 반환합니다.

## RestTemplate 사용 예제

### 1. RestTemplate 설정

`RestTemplateBuilder`를 통해 `RestTemplate` 객체를 구성하며, 연결 타임아웃(`setConnectTimeout`)과 읽기 타임아웃(`setReadTimeout`)을 각각 5초로 설정한 후 스프링 빈으로 등록합니다.

```java
import java.time.Duration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

  /**
   * RestTemplate 빈을 생성
   *
   * @return RestTemplate 객체
   */
  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplateBuilder()
            .setConnectTimeout(Duration.ofSeconds(5)) // 연결 타임아웃을 5초로 설정
            .setReadTimeout(Duration.ofSeconds(5)) // 읽기 타임아웃을 5초로 설정
            .build();
  }
}
```

### 2. HTTP 요청을 위한 유틸리티 클래스

HttpUtil 클래스는 `RestTemplate`을 활용하여 다양한 HTTP 메서드(GET, POST, PUT, DELETE)를 사용하여 HTTP 요청을 보내고, 서버로부터 응답을 받아 이를 DTO 객체로 반환하는 유틸리티 클래스입니다.

```java
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate를 사용한 HTTP 요청(GET, POST, PUT, DELETE)을 위한 유틸리티 클래스
 */
@AllArgsConstructor
@Component
public class HttpUtil {

  private final RestTemplate restTemplate;

  /**
   * HTTP 요청을 위한 HttpEntity를 생성
   *
   * @param headers 요청 헤더 정보(키-값 쌍)
   * @param body    요청 본문 객체
   * @return HttpEntity 객체
   */
  private <T> HttpEntity<T> createHttpEntity(Map<String, String> headers, T body) {
    HttpHeaders httpHeaders = new HttpHeaders();
    if (headers != null) {
      headers.forEach(httpHeaders::set);
    }
    return new HttpEntity<>(body, httpHeaders);
  }

  /**
   * GET 요청을 보내고 응답을 객체로 반환
   *
   * @param targetUrl    요청을 보낼 URL
   * @param headers      요청 헤더 정보
   * @param responseType 응답을 매핑할 클래스 타입
   * @return 응답 객체
   */
  public <T> ResponseEntity<T> sendGet(String targetUrl, Map<String, String> headers, Class<T> responseType) {
    HttpEntity<Void> entity = createHttpEntity(headers, null);
    return restTemplate.exchange(targetUrl, HttpMethod.GET, entity, responseType);
  }

  /**
   * POST 요청을 보내고 응답을 객체로 반환
   *
   * @param targetUrl    요청을 보낼 URL
   * @param postData     요청 본문 객체
   * @param headers      요청 헤더 정보
   * @param responseType 응답을 매핑할 클래스 타입
   * @return 응답 객체
   */
  public <T, R> ResponseEntity<R> sendPost(String targetUrl, T postData, Map<String, String> headers, Class<R> responseType) {
    HttpEntity<T> entity = createHttpEntity(headers, postData);
    return restTemplate.exchange(targetUrl, HttpMethod.POST, entity, responseType);
  }

  /**
   * PUT 요청을 보내고 응답을 객체로 반환
   *
   * @param targetUrl    요청을 보낼 URL
   * @param putData      요청 본문 객체
   * @param headers      요청 헤더 정보
   * @param responseType 응답을 매핑할 클래스 타입
   * @return 응답 객체
   */
  public <T, R> ResponseEntity<R> sendPut(String targetUrl, T putData, Map<String, String> headers, Class<R> responseType) {
    HttpEntity<T> entity = createHttpEntity(headers, putData);
    return restTemplate.exchange(targetUrl, HttpMethod.PUT, entity, responseType);
  }

  /**
   * DELETE 요청을 보내고 응답 객체를 반환
   *
   * @param targetUrl    요청을 보낼 URL
   * @param headers      요청 헤더 정보
   * @param responseType 응답을 매핑할 클래스 타입
   * @return 응답 객체
   */
  public <T> ResponseEntity<T> sendDelete(String targetUrl, Map<String, String> headers, Class<T> responseType) {
    HttpEntity<Void> entity = createHttpEntity(headers, null);
    return restTemplate.exchange(targetUrl, HttpMethod.DELETE, entity, responseType);
  }
}

```

## 3. 단위 테스트 작성

`HttpUtil` 클래스를 테스트하여 HTTP 요청(GET, POST, PUT, DELETE)이 정상적으로 동작하는지 확인합니다.

### 3.1 GET 요청 테스트

```java
@DisplayName("GET 요청: ID를 기준으로 포스트 조회 후 응답 ID 확인")
@Test
void testGetRequest() throws Exception {

  // Given
  Map<String, String> headers = new HashMap<>();
  headers.put(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);

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
```

이 테스트는 GET 요청을 보내고, 응답의 ID가 요청한 ID와 같은지 확인합니다.

### 3.2 POST 요청 테스트

```java
@DisplayName("POST 요청: 포스트 저장 후 응답의 title과 body 확인")
@Test
void testPostRequest() throws Exception {

  // Given
  Map<String, String> headers = new HashMap<>();
  headers.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

  PostDTO post = PostDTO.builder()
          .title("foo")
          .body("bar")
          .userId(1)
          .build();

  // When
  ResponseEntity<PostDTO> response = httpUtil.sendPost(TEST_POST_URL, post, headers,
          PostDTO.class);
  log.debug("response: {}", objectMapper.writeValueAsString(response.getBody()));

  // Then
  assertAll(
          () -> assertNotNull(response),
          () -> assertNotNull(response.getBody()),
          () -> assertEquals("foo", response.getBody().getTitle()),
          () -> assertEquals("bar", response.getBody().getBody())
  );
}
```

이 테스트는 POST 요청을 보내고, 응답의 title과 body가 요청한 값과 같은지 확인합니다.

### 3.3 PUT 요청 테스트

```java
@DisplayName("PUT 요청: 포스트 수정 후 응답의 title과 body 확인")
@Test
void testPutRequest() throws Exception {

  // Given
  Map<String, String> headers = new HashMap<>();
  headers.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

  PostDTO putData = PostDTO.builder()
          .id(1)
          .title("foo")
          .body("bar")
          .userId(1)
          .build();

  // When
  ResponseEntity<PostDTO> response = httpUtil.sendPut(TEST_PUT_URL, putData, headers,
          PostDTO.class);
  log.debug("response: {}", objectMapper.writeValueAsString(response.getBody()));

  // Then
  assertAll(
          () -> assertNotNull(response),
          () -> assertNotNull(response.getBody()),
          () -> assertEquals("foo", response.getBody().getTitle()),
          () -> assertEquals("bar", response.getBody().getBody())
  );
}
```

이 테스트는 PUT 요청을 보내고, 응답의 title과 body가 요청한 값과 같은지 확인합니다.

### 3.4 DELETE 요청 테스트

```java
@DisplayName("DELETE 요청: 포스트 삭제 후 응답이 빈 값인지 확인")
@Test
void testDeleteRequest() throws Exception {

  // Given
  Map<String, String> headers = new HashMap<>();
  headers.put(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);

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
```

이 테스트는 DELETE 요청을 보내고, 응답이 빈 값인지 확인합니다.



## 참고 자료
- [Spring 공식 문서 - RestTemplate](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/client/RestTemplate.html)
- [Guide to RestTemplate](https://www.baeldung.com/rest-template)
