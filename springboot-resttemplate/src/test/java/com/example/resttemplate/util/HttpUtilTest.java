package com.example.resttemplate.util;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

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

  @Order(2)
  @DisplayName("POST 요청: 포스트 저장 후 응답의 title과 body 확인")
  @Test
  public void testPostRequest() throws Exception {

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

  @Order(3)
  @DisplayName("PUT 요청: 포스트 수정 후 응답의 title과 body 확인")
  @Test
  public void testPutRequest() throws Exception {

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

  @Order(4)
  @DisplayName("DELETE 요청: 포스트 삭제 후 응답이 빈 값인지 확인")
  @Test
  public void testDeleteRequest() throws Exception {

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
