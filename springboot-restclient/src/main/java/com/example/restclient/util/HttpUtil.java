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
