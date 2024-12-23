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

  // Connection Pool 설정 값
  private static final int MAX_TOTAL_CONNECTIONS = 100; // 최대 전체 커넥션 수
  private static final int MAX_CONNECTIONS_PER_ROUTE = 10; // 특정 호스트(경로)별 최대 커넥션 수
  private static final int MAX_IDLE_TIME = 10; // 유휴 연결 유지 시간 (초 단위)

  // Retry 설정 값
  private static final int MAX_RETRIES = 1; // 요청 실패 시 재시도 횟수
  private static final long RETRY_INTERVAL_IN_SECONDS = 1L; // 재시도 간격 (초 단위)

  // Timeout 설정 값
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
   * @return 설정된 HttpClient 객체
   */
  @Bean
  public HttpClient httpClient() {
    return HttpClients.custom()
        .setConnectionBackoffStrategy(new DefaultBackoffStrategy())
        .setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy())
        .setRetryStrategy(buildRetryStrategy())
        .setConnectionReuseStrategy(DefaultConnectionReuseStrategy.INSTANCE)
        .setDefaultRequestConfig(buildRequestConfig())
        .setConnectionManager(buildConnectionManager())
        .evictExpiredConnections()
        .evictIdleConnections(TimeValue.ofSeconds(MAX_IDLE_TIME))
        .build();
  }

  /**
   * Connection Manager를 생성
   *
   * @return 설정된 PoolingHttpClientConnectionManager 객체
   */
  private PoolingHttpClientConnectionManager buildConnectionManager() {
    PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
    connectionManager.setMaxTotal(MAX_TOTAL_CONNECTIONS);
    connectionManager.setDefaultMaxPerRoute(MAX_CONNECTIONS_PER_ROUTE);
    return connectionManager;
  }

  /**
   * Request Configuration를 생성
   *
   * @return 설정된 RequestConfig 객체
   */
  private RequestConfig buildRequestConfig() {
    return RequestConfig.custom()
        .setResponseTimeout(RESPONSE_TIMEOUT, TimeUnit.SECONDS)
        .setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT, TimeUnit.SECONDS)
        .build();
  }

  /**
   * Retry Strategy를 생성
   *
   * @return 설정된 DefaultHttpRequestRetryStrategy 객체
   */
  private DefaultHttpRequestRetryStrategy buildRetryStrategy() {
    return new DefaultHttpRequestRetryStrategy(
        MAX_RETRIES,
        TimeValue.ofSeconds(RETRY_INTERVAL_IN_SECONDS)
    );
  }
}
