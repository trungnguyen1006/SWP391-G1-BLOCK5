package com.hotelmanage.entity;


import com.hotelmanage.entity.Enum.UserRole;
import com.hotelmanage.entity.Enum.UserStatus;
import com.hotelmanage.entity.booking.Booking;
import com.hotelmanage.entity.emailNotification.EmailNotification;
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
@SQLDelete(sql = "UPDATE users SET deleted_at = CURRENT_TIMESTAMP WHERE user_id = ?")
@Where(clause = "deleted_at IS NULL")
@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(name = "uk_users_username", columnNames = {"username"}),
        @UniqueConstraint(name = "uk_users_email", columnNames = {"email"})
})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    @EqualsAndHashCode.Include
    Long id;

    @Column(name = "username", nullable = false, length = 255)
    String username;

    @Column(name = "password", nullable = false, length = 255)
    String password;

    @Column(name = "email", nullable = false, length = 255)
    String email;

    @Column(name = "phone", length = 32)
    String phone;

    @Column(name = "address", length = 500)
    String address;

    @Column(name = "avatar_url", length = 1000)
    String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_id", nullable = false, length = 32)
    UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    UserStatus status;

    @Column(name = "deleted_at")
    LocalDateTime deletedAt;

    // Relationships
    @OneToMany(mappedBy = "user")
    Set<Booking> bookings = new HashSet<>();

    @OneToMany(mappedBy = "user")
    Set<EmailNotification> emailNotifications = new HashSet<>();


}


