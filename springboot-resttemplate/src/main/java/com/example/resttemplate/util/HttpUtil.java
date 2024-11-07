package com.example.resttemplate.util;

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
  public <T> ResponseEntity<T> sendGet(String targetUrl, Map<String, String> headers,
      Class<T> responseType) {
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
  public <T, R> ResponseEntity<R> sendPost(String targetUrl, T postData,
      Map<String, String> headers, Class<R> responseType) {
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
  public <T, R> ResponseEntity<R> sendPut(String targetUrl, T putData, Map<String, String> headers,
      Class<R> responseType) {
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
  public <T> ResponseEntity<T> sendDelete(String targetUrl, Map<String, String> headers,
      Class<T> responseType) {
    HttpEntity<Void> entity = createHttpEntity(headers, null);
    return restTemplate.exchange(targetUrl, HttpMethod.DELETE, entity, responseType);
  }
}
