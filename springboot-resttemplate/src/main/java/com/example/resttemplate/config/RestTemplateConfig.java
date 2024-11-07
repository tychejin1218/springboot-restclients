package com.example.resttemplate.config;

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
