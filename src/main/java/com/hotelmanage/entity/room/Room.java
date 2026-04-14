package com.hotelmanage.entity.room;

import com.hotelmanage.entity.Enum.RoomStatus;
import com.hotelmanage.entity.booking.Booking;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "room", indexes = {
        @Index(name = "idx_room_roomtype", columnList = "room_type_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id")
    private Integer roomId;

    @NotBlank(message = "Số phòng không được để trống")
    @Pattern(regexp = "^[0-9]+$", message = "Số phòng phải là số dương hợp lệ")
    @Size(max = 25, message = "Số phòng không được vượt quá 25 ký tự")
    @Column(name = "room_number", nullable = false, unique = true, length = 25)
    private String roomNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RoomStatus status = RoomStatus.AVAILABLE;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "room_type_id", nullable = false)
    private RoomType roomType;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Booking> bookings;
}
