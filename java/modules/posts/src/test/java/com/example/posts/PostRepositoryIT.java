package com.example.posts;

import com.example.cockroach_db.CockroachIntegrationTest;
import com.example.users.api.repository.UserId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class PostRepositoryIT extends CockroachIntegrationTest {
    // TODO: replace with repository, implement other tests
    @Autowired
    private PostService postService;

    private final UserId authorId = new UserId(UUID.randomUUID());
    private final String text = "Hello, world!";

    /** The created post should return its id */
    @Test
    void testCreate() {
        PostId id = postService.insertAndReturnId(authorId, text);
        assertThat(id).isNotNull();
    }

    /** The created post should be found */
    @Test
    void testRead() {
        PostId id = postService.insertAndReturnId(authorId, text);
        Post post = postService.getPost(id);

        assertThat(post).isNotNull();
        assertThat(post.authorId() == authorId);
        assertThat(Objects.equals(post.text(), text));
        assertThat(post.id() == id);
    }

    /** The update should change the data of the post */
    @Test
    void testUpdate(){

    }

    /** The deletion should mark the post as deleted */
    @Test
    void testDelete(){

    }

    /** Creating a post with an invalid author id should throw an error */
    @Test
    void testUserForeignKey(){

    }
}
