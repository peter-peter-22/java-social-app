package com.example.posts.api;

import com.example.posts.Post;
import com.example.posts.PostId;
import com.example.posts.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/posts")
@Tag(name = "posts", description = "Post management APIs")
class PostController {
    private final PostService postService;

    public PostController(
            @Autowired PostService postService
    ) {
        this.postService = postService;
    }

    @GetMapping
    @Operation(summary = "Get all users")
    public ResponseEntity<Post> example() {
        return ResponseEntity.ok(postService.getPost(new PostId(UUID.randomUUID())));
    }
}
