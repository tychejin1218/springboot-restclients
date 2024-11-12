package com.example.httpinterface.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.example.httpinterface.dto.PostDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest
class PostServiceTest {

  @Autowired
  PostService postService;

  @Autowired
  ObjectMapper objectMapper;

  @Order(1)
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

  @Order(2)
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

  @Order(3)
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

  @Order(4)
  @DisplayName("DELETE 요청: 포스트 삭제 후 응답이 빈 값인지 확인")
  @Test
  public void testDeleteRequest() throws Exception {

    // Given
    int postId = 1;

    // When
    PostDto.Response response = postService.deletePost(postId);
    log.debug("response: {}", objectMapper.writeValueAsString(response.getBody()));

    // Then
    assertAll(
        () -> assertNotNull(response),
        () -> assertEquals(null, response.getTitle()),
        () -> assertEquals(null, response.getBody())
    );
  }
}
