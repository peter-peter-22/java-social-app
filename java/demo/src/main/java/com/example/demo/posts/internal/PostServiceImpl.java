package com.example.demo.posts.internal;

import com.example.demo.posts.Post;
import com.example.demo.posts.PostId;
import com.example.demo.posts.PostService;
import com.example.demo.users.UserId;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
class PostServiceImpl implements PostService {
    public @NotNull Post createPost(@NotNull UserId authorId, @NotNull String text) {
        return new Post(new PostId("1"),new UserId("1"), "1", Instant.now());
    }

    public @Nullable Post getPost(@NotNull PostId id) {
        return null;
    }

    public void b(@NotNull UserId authorId, @Nullable UserId authorId2, UserId authorId3) {
        System.out.println(authorId.id());
        System.out.println(authorId2.id());
        System.out.println(authorId3.id());
    }
}
