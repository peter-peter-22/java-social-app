package com.example.posts.internal.post_service;

import com.example.posts.Post;
import com.example.posts.PostId;
import com.example.posts.PostService;
import com.example.users_api.repository.UserId;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
class PostServiceImpl implements PostService {
    private final PostRepository postRepository;

    private static @NotNull Post entityToDTO(PostEntity e) {
        return new Post(
                new PostId(e.id()),
                new UserId(e.authorId()),
                e.body(),
                Instant.now()
        );
    }

    public @NotNull PostId insertAndReturnId(@NotNull UserId authorId, @NotNull String text) {
        return new PostId(postRepository.insertAndReturnId(authorId.get(), text));
    }

    public @Nullable Post getPost(@NotNull PostId id) {
        return postRepository.findById(id.get())
                .map(PostServiceImpl::entityToDTO)
                .orElse(null);
    }
}
