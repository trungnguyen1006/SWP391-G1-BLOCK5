package com.hotelmanage.entity.emailNotification;

import com.hotelmanage.entity.Enum.NotificationStatus;
import com.hotelmanage.entity.User;
import com.hotelmanage.entity.booking.Booking;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@SQLDelete(sql = "UPDATE email_notification SET deleted_at = CURRENT_TIMESTAMP WHERE notification_id = ?")
@Where(clause = "deleted_at IS NULL")
@Entity
@Table(name = "email_notification")
public class EmailNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    @EqualsAndHashCode.Include
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    Booking booking;

    @Column(name = "subject", nullable = false, length = 255)
    String subject;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    NotificationStatus status;

    @Column(name = "deleted_at")
    LocalDateTime deletedAt;
}


