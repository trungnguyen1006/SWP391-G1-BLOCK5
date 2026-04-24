package com.hotelmanage.entity.restaurant;

import com.hotelmanage.entity.Enum.BookingShift;
import com.hotelmanage.entity.Enum.RestaurantBookingStatus;
import com.hotelmanage.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Where;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@SQLDelete(sql = "UPDATE restaurant_booking SET deleted_at = CURRENT_TIMESTAMP WHERE booking_id = ?")
@Where(clause = "deleted_at IS NULL")
@Entity
@Table(name = "restaurant_booking")
public class RestaurantBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id")
    @EqualsAndHashCode.Include
    Long bookingId;

    // Khách hàng (có thể là CUSTOMER hoặc GUEST)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
    User user;

    // Nhà hàng được đặt
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "restaurant_id", nullable = false,
            foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
    Restaurant restaurant;

    @Column(name = "booking_date", nullable = false)
    LocalDate bookingDate;

    // Ca đặt bàn: SANG / TRUA / CHIEU / TOI
    @Enumerated(EnumType.STRING)
    @Column(name = "booking_shift", nullable = false, length = 10)
    BookingShift bookingShift;

    @Column(name = "number_of_guests", nullable = false)
    Integer numberOfGuests;

    @Column(name = "special_request", length = 500)
    String specialRequest;

    @Column(name = "cancel_reason", length = 500)
    String cancelReason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    RestaurantBookingStatus status = RestaurantBookingStatus.PENDING;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    LocalDateTime deletedAt;
}
