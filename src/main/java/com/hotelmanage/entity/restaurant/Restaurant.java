package com.hotelmanage.entity.restaurant;

import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@SQLDelete(sql = "UPDATE restaurant SET deleted_at = CURRENT_TIMESTAMP WHERE restaurant_id = ?")
@Where(clause = "deleted_at IS NULL")
@Entity
@Table(name = "restaurant")
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "restaurant_id")
    @EqualsAndHashCode.Include
    Long id;

    @Column(name = "name", nullable = false, length = 255)
    String name;

    @Column(name = "image_url", length = 1000)
    String imageUrl;

    @Column(name = "cuisine_type", length = 100)
    String cuisineType;

    @Column(name = "opening_hours", length = 255)
    String openingHours;

    @Column(name = "price_range", length = 64)
    String priceRange;

    @Column(name = "promotion_text", length = 255)
    String promotionText;

    @Column(name = "description", length = 2000)
    String description;

    @Column(name = "contact_info", length = 255)
    String contactInfo;

    @OneToMany(mappedBy = "restaurant")
    Set<Menu> menus = new HashSet<>();

    @Column(name = "deleted_at")
    LocalDateTime deletedAt;
}



