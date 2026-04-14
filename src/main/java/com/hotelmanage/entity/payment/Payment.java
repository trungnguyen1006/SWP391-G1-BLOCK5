package com.hotelmanage.entity.payment;

import com.hotelmanage.entity.Enum.PaymentStatus;
import com.hotelmanage.entity.booking.Booking;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long paymentId;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "payment_date", nullable = false)
    private LocalDateTime paymentDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_payment_booking",
                    value = ConstraintMode.NO_CONSTRAINT))
    private Booking booking;

    @Column(name = "transaction_id", nullable = false, length = 255)
    private String transactionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20)
    private PaymentStatus paymentStatus;

    @Column(name = "receipt_url", length = 1000)
    private String receiptUrl;

    @Transient
    private LocalDateTime expireTime;

    public LocalDateTime getExpireTime() {
        if (paymentDate == null) {
            return null;
        }
        return paymentDate.plusMinutes(2);
    }
}
