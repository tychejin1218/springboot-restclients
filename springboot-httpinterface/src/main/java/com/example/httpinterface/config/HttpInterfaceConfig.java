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
