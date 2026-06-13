package com.example.posts;

import com.example.cockroach_db.CockroachIntegrationTest;
import com.example.users.repository.UserId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
public class PostServiceIT extends CockroachIntegrationTest {

    @Autowired
    private PostService postService;

    private final UserId authorId = new UserId(UUID.randomUUID());
    private final String text = "Hello, world!";

    @Test
    void testCreatePost() {
        // The created post should return its id
        PostId id = postService.insertAndReturnId(authorId, text);
        assertThat(id).isNotNull();
    }

    @Test
    void testReadPost() {
        // The created post should be found
        PostId id = postService.insertAndReturnId(authorId, text);
        Post post = postService.getPost(id);

        assertThat(post).isNotNull();
        assertThat(post.authorId() == authorId);
        assertThat(Objects.equals(post.text(), text));
        assertThat(post.id() == id);
    }
}
