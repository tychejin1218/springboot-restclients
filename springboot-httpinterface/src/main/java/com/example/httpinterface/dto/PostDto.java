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
