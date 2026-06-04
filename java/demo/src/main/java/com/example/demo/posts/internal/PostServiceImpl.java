package com.example.demo.posts.internal;

import com.example.demo.posts.Post;
import com.example.demo.posts.PostId;
import com.example.demo.posts.PostService;
import com.example.demo.users.UserId;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
class PostServiceImpl implements PostService {
    private final PostRepository postRepository;

    public PostServiceImpl(
            @Autowired PostRepository postRepository
    ) {
        this.postRepository = postRepository;
    }

    public @NotNull PostId insertAndReturnId(@NotNull UserId authorId, @NotNull String text) {
        return postRepository.create(authorId.id(),text);
    }

    public @Nullable Post getPost(@NotNull PostId id) {
        return postRepository.findById(id)
                .map(found -> new Post(
                        found.id(),
                        found.authorId(),
                        found.body(),
                        Instant.now()
                ))
                .orElse(null);
    }
}
