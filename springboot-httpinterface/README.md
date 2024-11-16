# Spring RestClient, HttpInterface를 활용한 HTTP 요청

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

- `@RequestHeader` : 요청 헤더 이름과 값을 추가 (`Map` 또는 `MultiValueMap`)
- `@PathVariable` : 요청 URL에 포함된 경로 변수를 메서드 매개변수에 대체
- `@RequestBody` : 직렬화할 객체 또는 `Mono`나 `Flux`와 같은 반응형 스트림으로 요청 본문을 제공
- `@RequestParam` : 요청 매개변수 이름과 값을 추가 (`Map` 또는 `MultiValueMap`)
- `@CookieValue` : 쿠키 이름과 값을 추가 (`Map` 또는 `MultiValueMap`)

## RestClient, HttpInterface 사용 예제

Spring Boot를 사용하면 Gradle 빌드 파일에 `spring-boot-starter-web` 의존성을 추가하여 `HttpInterface`를 사용할 수 있습니다.
`HttpInterface`를 사용하면 HTTP 요청 인터페이스를 정의하고 자동으로 프록시 객체를 생성하여 HTTP 요청을 수행할 수 있습니다.

`RestClient`에 대한 추가적인 설정이 필요한 경우, `ClientHttpRequestFactorySettings`를 사용하여 연결 타임아웃(
`withConnectTimeout`)과 읽기 타임아웃(`withReadTimeout`)을 각각 5초로 설정한 후, 이를 스프링 빈으로 등록할 수 있습니다.

이 예제에서는 `RestClient`와 `HttpInterface`를 결합하여 JSONPlaceholder API와 통신하는 방법을 보여드립니다. 이를 위해 먼저
`RestClient` 설정을 등록하고, 이를 사용하는 HTTP 인터페이스 서비스를 생성합니다.

### 1. Gradle 의존성 추가

```groovy
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
}
```

### 2. RestClient 설정 및 HttpInterface 구성

```java
package com.example.httpinterface.config;

import com.example.httpinterface.service.PostService;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Slf4j
@Configuration
public class HttpInterfaceConfig {

  private static final String JSON_PLACEHOLDER_URL = "https://jsonplaceholder.typicode.com";

  /**
   * JSONPlaceholder API를 위한 PostService 빈을 생성
   * <p> RestClient를 기반으로 JSONPlaceholder API와 통신할 PostService 인스턴스를 생성</p>
   *
   * @param restClient RestClient 객체
   * @return JSONPlaceholder API와 통신할 PostService 인스턴스
   */
  @Bean
  PostService jsonPlaceholderInterface(RestClient restClient) {

    // RestClient 객체를 사용하여 JSONPlaceholder API의 기본 URL을 설정
    RestClient postRestClient = restClient
        .mutate()
        .baseUrl(JSON_PLACEHOLDER_URL)
        .build();

    // RestClientAdapter 생성의 인스턴스를 생성
    RestClientAdapter restClientAdapter = RestClientAdapter.create(postRestClient);

    // HttpServiceProxyFactory를 사용하여 HTTP 인터페이스 프록시를 생성
    HttpServiceProxyFactory httpServiceProxyFactory = HttpServiceProxyFactory
        .builderFor(restClientAdapter)
        .build();

    return httpServiceProxyFactory.createClient(PostService.class);
  }

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

### 3. 서비스 인터페이스와 DTO 클래스

#### 3_1. 서비스 인터페이스

JSONPlaceholder API와 통신하기 위한 `PostService` 인터페이스를 정의합니다.

```java
package com.example.httpinterface.service;

import com.example.httpinterface.dto.PostDto;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

@HttpExchange(url = "")
public interface PostService {

  @GetExchange("/posts/{id}")
  PostDto.Response getPost(@PathVariable int id);

  @PostExchange("/posts")
  PostDto.Response createPost(@RequestBody PostDto.Request request);

  @PutExchange("/posts/{id}")
  PostDto.Response updatePost(@PathVariable int id, @RequestBody PostDto.Request request);

  @DeleteExchange("/posts/{id}")
  PostDto.Response deletePost(@PathVariable int id);
}
```

#### 3_2. DTO 클래스

요청 및 응답 데이터를 위한 DTO 클래스 `PostDto`를 정의합니다.

```java
package com.example.httpinterface.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class PostDto {

  @Getter
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Request {

    private int id;
    private String title;
    private String body;
    private int userId;
  }

  @Getter
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Response {

    private int id;
    private String title;
    private String body;
    private int userId;
  }
}
```

### 4. 단위 테스트 작성

RestClient, HttpInterface를 활용한 HTTP 요청 (GET, POST, PUT, DELETE)이 정상적으로 동작하는지 확인합니다.

#### 4_1. GET 요청 테스트

GET 요청을 보내고, 응답의 ID가 요청한 ID와 같은지 확인합니다.

```java

@DisplayName("GET 요청: ID를 기준으로 포스트 조회 후 응답 ID 확인")
@Test
public void testGetRequest() throws Exception {

  // Given
  int postId = 1;

  // When
  PostDto.Response response = postService.getPost(postId);
  log.debug("response: {}", objectMapper.writeValueAsString(response));

  // Then
  assertAll(
      () -> assertNotNull(response),
      () -> assertEquals(1, response.getId())
  );
}
```

#### 4_2. POST 요청 테스트

POST 요청을 보내고, 응답의 title과 body가 요청한 값과 같은지 확인합니다.

```java

@DisplayName("POST 요청: 포스트 저장 후 응답의 title과 body 확인")
@Test
public void testPostRequest() throws Exception {

  // Given
  PostDto.Request postData = PostDto.Request.builder()
      .title("foo")
      .body("bar")
      .userId(1)
      .build();

  // When
  PostDto.Response response = postService.createPost(postData);
  log.debug("response: {}", objectMapper.writeValueAsString(response));

  // Then
  assertAll(
      () -> assertNotNull(response),
      () -> assertEquals("foo", response.getTitle()),
      () -> assertEquals("bar", response.getBody())
  );
}
```

#### 4_3. PUT 요청 테스트

PUT 요청을 보내고, 응답의 title과 body가 요청한 값과 같은지 확인합니다.

```java

@DisplayName("PUT 요청: 포스트 수정 후 응답의 title과 body 확인")
@Test
public void testPutRequest() throws Exception {

  // Given
  int postId = 1;
  PostDto.Request putData = PostDto.Request.builder()
      .id(postId)
      .title("foo")
      .body("bar")
      .userId(1)
      .build();

  // When
  PostDto.Response response = postService.updatePost(postId, putData);
  log.debug("response: {}", objectMapper.writeValueAsString(response));

  // Then
  assertAll(
      () -> assertNotNull(response),
      () -> assertEquals("foo", response.getTitle()),
      () -> assertEquals("bar", response.getBody())
  );
}
```

#### 4_4. DELETE 요청 테스트

DELETE 요청을 보내고, 응답이 빈 값인지 확인합니다.

```java

@DisplayName("DELETE 요청: 포스트 삭제 후 응답이 빈 값인지 확인")
@Test
public void testDeleteRequest() throws Exception {

  // Given
  int postId = 1;

  // When
  PostDto.Response response = postService.deletePost(postId);
  log.debug("response: {}", objectMapper.writeValueAsString(response));

  // Then
  assertAll(
      () -> assertNotNull(response),
      () -> assertEquals(null, response.getTitle()),
      () -> assertEquals(null, response.getBody())
  );
}
```

## 참고 자료

- [Spring 공식 문서 - REST Clients](https://docs.spring.io/spring-framework/reference/integration/rest-clients.html)
- [HTTP Interface in Spring](https://www.baeldung.com/spring-6-http-interface)
