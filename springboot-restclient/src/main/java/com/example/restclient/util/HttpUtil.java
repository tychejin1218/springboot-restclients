package com.example.restclient.util;

import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

/**
 * RestTemplate를 사용한 HTTP 요청(GET, POST, PUT, DELETE)을 위한 유틸리티 클래스
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
        .headers(
            httpHeaders -> {
              httpHeaders.addAll(headers);
            }
        )
        .retrieve()
        .toEntity(responseType);
  }
}
