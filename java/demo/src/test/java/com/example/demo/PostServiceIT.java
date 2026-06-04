package com.example.demo;

import com.example.demo.posts.Post;
import com.example.demo.posts.PostId;
import com.example.demo.posts.PostService;
import com.example.demo.users.User;
import com.example.demo.users.UserId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.CockroachContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
public class PostServiceIT {

    @Container
    private static final CockroachContainer cockroach = new CockroachContainer("cockroachdb/cockroach:v25.3.2");

    @DynamicPropertySource
    static void registerDynamicProperties(DynamicPropertyRegistry registry) {
        System.out.println("[DEBUG_LOG] url");
        System.out.println( cockroach.getJdbcUrl());

        registry.add("spring.datasource.url", cockroach::getJdbcUrl);
        registry.add("spring.datasource.username", cockroach::getUsername);
        registry.add("spring.datasource.password", cockroach::getPassword);
    }

    @Autowired
    private PostService postService;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    private final UserId authorId = new UserId(UUID.randomUUID());
    private final String text = "Hello, world!";

    @Test
    void testCreatePost() {
        // Test if
        PostId id = postService.insertAndReturnId(authorId, text);
        assertThat(id).isNotNull();
    }

    @Test
    void testReadPost() {
        PostId id = postService.insertAndReturnId(authorId, text);
        Post post = postService.getPost(id);

        assertThat(post).isNotNull();
        assertThat(post.authorId() == authorId);
        assertThat(Objects.equals(post.text(), text));
        assertThat(post.id() == id);
    }
}
