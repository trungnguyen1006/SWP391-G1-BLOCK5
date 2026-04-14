package com.hotelmanage.entity.room;

import jakarta.persistence.*;
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

