package com.example.demo.posts.internal;

import com.example.demo.posts.PostId;
import com.example.demo.users.UserId;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("posts")
public record PostEntity(
        @Id
        PostId id,
        // TODO do we need this?
        @Column("author_id") UserId authorId,
        String body
) {
}
