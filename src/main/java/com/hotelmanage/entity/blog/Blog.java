package com.hotelmanage.entity.blog;

import jakarta.persistence.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@SQLDelete(sql = "UPDATE blog SET deleted_at = CURRENT_TIMESTAMP WHERE blog_id = ?")
@Where(clause = "deleted_at IS NULL")
@Entity
@Table(name = "blog")
public class Blog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "blog_id")
    @EqualsAndHashCode.Include
    Long id;

    @Column(name = "title", nullable = false, length = 255)
    String title;

    @Column(name = "image_url", length = 1000)
    String imageUrl;

    @Column(name = "excerpt", length = 500)
    String excerpt;

    @Lob
    @Column(name = "content", columnDefinition = "LONGTEXT")
    String content;

    @Column(name = "deleted_at")
    LocalDateTime deletedAt;
}


