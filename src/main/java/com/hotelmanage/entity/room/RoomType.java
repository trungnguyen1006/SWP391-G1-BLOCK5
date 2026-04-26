package com.hotelmanage.entity.room;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "room_type")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_type_id")
    private Integer roomTypeId;

    @NotBlank(message = "Tên loại phòng không được để trống")
    @Column(name = "room_type_name", nullable = false)
    private String roomTypeName;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "device", columnDefinition = "TEXT")
    private String device;

    @Column(name = "utilities", columnDefinition = "TEXT")
    private String utilities;

    @NotNull(message = "Giá không được để trống")
    @Min(value = 1000, message = "Giá phải lớn hơn 1,000 VNĐ")
    @Max(value = 1000000000, message = "Giá phải nhỏ hơn 1,000,000,000 VNĐ")
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "amount_person", nullable = false)
    private Integer amountPerson;

    @OneToMany(mappedBy = "roomType")
    private List<Room> rooms;

    @OneToMany(mappedBy = "roomType", fetch = FetchType.EAGER)
    private List<RoomTypeImage> images;

    @Transient
    private Integer availableCount;


}

