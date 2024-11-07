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
