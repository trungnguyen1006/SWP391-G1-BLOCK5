package com.hotelmanage.entity.restaurant;

import jakarta.persistence.*;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@SQLDelete(sql = "UPDATE menu SET deleted_at = CURRENT_TIMESTAMP WHERE menu_id = ?")
@Where(clause = "deleted_at IS NULL")
@Entity
@Table(name = "menu")
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "menu_id")
    @EqualsAndHashCode.Include
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotFound(action = NotFoundAction.IGNORE)
    @JoinColumn(name = "restaurant_id", nullable = false)
    Restaurant restaurant;

    @Column(name = "item_name", nullable = false, length = 255)
    String itemName;

    @Column(name = "image_url", length = 1000)
    String imageUrl;

    @Column(name = "price", precision = 12, scale = 0)
    BigDecimal price;

    @Column(name = "deleted_at")
    LocalDateTime deletedAt;
}
