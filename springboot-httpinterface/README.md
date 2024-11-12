# Spring HttpInterface를 활용한 HTTP 요청

## HttpInterface란?

Spring 6에서는 HTTP 클라이언트를 사용하기 위한 새로운 방법으로 `HttpInterface`를 도입하였습니다. 이는 Spring HTTP 인터페이스를 통해 직관적이고
간편한 방법으로 HTTP 요청을 수행할 수 있게 합니다. 기존의 `RestTemplate`이나 `WebClient`와 달리, `HttpInterface`는 인터페이스를 선언하고
이를 구현하는 방식을 사용합니다.

`HttpInterface`는 애노테이션과 프록시(Proxy) 패턴을 활용하여 HTTP 요청을 마치 메서드 호출처럼 사용할 수 있게 해줍니다. 이는 가독성이 좋고 테스트 편리하게
만들어 줍니다.

## 주요 애노테이션 및 기능

### HTTP 메서드별 애노테이션

- `@HttpExchange` : HTTP 인터페이스와 그 요청에 적용할 수 있는 기본 애노테이션으로, 인터페이스 수준에서 적용하는 경우 모든 요청에 공통된 속성을 지정하는데
  유용
- `@PostExchange` : HTTP POST 요청에 대한 애노테이션
- `@PutExchange` : HTTP PUT 요청에 대한 애노테이션
- `@PatchExchange` : HTTP PATCH 요청에 대한 애노테이션
- `@DeleteExchange` : HTTP DELETE 요청에 대한 애노테이션

### 메서드 매개변수

- `URI`: 주석 속성을 재정의하여 요청에 대한 URL을 동적으로 설정합니다.
- `HttpMethod`: 주석 속성을 재정의하여 요청에 대한 HTTP 메서드를 동적으로 설정합니다.
-
- `@RequestHeader` : 요청 헤더 이름과 값을 추가 (`Map` 또는 `MultiValueMap`)
- `@PathVariable` : 요청 URL에 포함된 경로 변수를 메서드 매개변수에 대체
- `@RequestBody` : 직렬화할 객체 또는 `Mono`나 `Flux`와 같은 반응형 스트림 게시자로 요청 본문을 제공
- `@RequestParam` :  요청 매개변수 이름과 값을 추가 (`Map` 또는 `MultiValueMap`)
- `@CookieValue` : 쿠키 이름과 값을 추가 (`Map` 또는 `MultiValueMap`)

## HttpInterface 사용 예제

Spring Boot를 사용하면 Gradle 빌드 파일에 별도로 `RestClient` 의존성을 추가하지 않고도 HTTP 요청을 보낼 수 있습니다. 이는 Spring Boot의
자동 구성(auto-config) 기능 덕분에 필요한 라이브러리를 자동으로 추가하고 구성해주기 때문입니다. 다만, 구체적인 설정(예: 타임아웃 값 조정 등)이 필요한 경우에는 직접
설정을 추가해야 합니다.

추가적인 설정이 필요한 경우, `ClientHttpRequestFactorySettings`를 사용하여 연결 타임아웃(`withConnectTimeout`)과 읽기 타임아웃(
`withReadTimeout`)을 각각 5초로 설정한 후, 이를 스프링 빈으로 등록할 수 있습니다. 이렇게 하려면 먼저 Gradle 빌드 파일에 아래와 같이
`spring-boot-starter-web` 의존성을 추가해야 합니다.

### 1. RestClient 설정

```groovy
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
}
```

```java

@Configuration
public class RestClientConfig {

  /**
   * RestClient 빈을 생성
   *
   * @return RestClient 객체
   */
  @Bean
  public RestClient restClient() {
    return RestClient.builder()
        .requestFactory(customRequestFactory())
        .build();
  }

  /**
   * ClientHttpRequestFactory를 생성
   *
   * @return ClientHttpRequestFactory
   */
  ClientHttpRequestFactory customRequestFactory() {
    ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.DEFAULTS
        .withConnectTimeout(Duration.ofSeconds(5))  // 연결 타임아웃을 5초로 설정
        .withReadTimeout(Duration.ofSeconds(5)); // 읽기 타임아웃을 5초로 설정
    return ClientHttpRequestFactories.get(settings);
  }
}
```

### 2. HTTP 요청을 위한 유틸리티 클래스

HttpUtil 클래스는 `RestClient`을 활용하여 다양한 HTTP 메서드(GET, POST, PUT, DELETE)를 사용하여 HTTP 요청을 보내고, 서버로부터
응답을 받아 이를 DTO 객체로 반환하는 유틸리티 클래스입니다.

```java
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
        .headers(httpHeaders -> httpHeaders.addAll(headers))
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
        .headers(httpHeaders -> httpHeaders.addAll(headers))
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
        .headers(httpHeaders -> httpHeaders.addAll(headers))
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
        .headers(httpHeaders -> httpHeaders.addAll(headers))
        .retrieve()
        .toEntity(responseType);
  }
}
```

## 3. 단위 테스트 작성

`HttpUtil` 클래스를 테스트하여 HTTP 요청(GET, POST, PUT, DELETE)이 정상적으로 동작하는지 확인합니다.

### 3_1. GET 요청 테스트

GET 요청을 보내고, 응답의 ID가 요청한 ID와 같은지 확인합니다.

```java

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
```

### 3_2. POST 요청 테스트

POST 요청을 보내고, 응답의 title과 body가 요청한 값과 같은지 확인합니다.

```java

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
```

### 3_3. PUT 요청 테스트

PUT 요청을 보내고, 응답의 title과 body가 요청한 값과 같은지 확인합니다.

```java

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
```

### 3_4. DELETE 요청 테스트

DELETE 요청을 보내고, 응답이 빈 값인지 확인합니다.

```java

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
```

## 참고 자료

- [Spring 공식 문서 - REST Clients](https://docs.spring.io/spring-framework/reference/integration/rest-clients.html)
- [HTTP Interface in Spring](https://www.baeldung.com/spring-6-http-interface)
