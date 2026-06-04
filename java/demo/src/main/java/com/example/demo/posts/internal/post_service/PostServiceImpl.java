package com.example.demo.posts.internal.post_service;

import com.example.demo.posts.Post;
import com.example.demo.posts.PostId;
import com.example.demo.posts.PostService;
import com.example.demo.users.UserId;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
class PostServiceImpl implements PostService {
    private final PostRepository postRepository;

    public PostServiceImpl(
            @Autowired PostRepository postRepository
    ) {
        this.postRepository = postRepository;
    }

    private static @NotNull Post entityToDTO(PostEntity e) {
        return new Post(
                new PostId(e.id()),
                new UserId(e.authorId()),
                e.body(),
                Instant.now()
        );
    }

    public @NotNull PostId insertAndReturnId(@NotNull UserId authorId, @NotNull String text) {
        return new PostId(postRepository.insertAndReturnId(authorId.id(), text));
    }

    public @Nullable Post getPost(@NotNull PostId id) {
        return postRepository.findById(id.id())
                .map(PostServiceImpl::entityToDTO)
                .orElse(null);
    }
}
