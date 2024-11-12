package com.example.httpinterface.service;

import com.example.httpinterface.dto.PostDto;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

@HttpExchange
public interface PostService {

  @GetExchange("/posts/{id}")
  PostDto.Response getPost(@PathVariable int id);

  @PostExchange("/posts")
  PostDto.Response createPost(@RequestBody PostDto.Request request);

  @PutExchange("/posts/{id}")
  PostDto.Response updatePost(@PathVariable int id, @RequestBody PostDto.Request request);

  @DeleteExchange("/posts/{id}")
  PostDto.Response deletePost(@PathVariable int id);
}
