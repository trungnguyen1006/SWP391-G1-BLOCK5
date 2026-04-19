package com.hotelmanage.entity.amenity;

import com.hotelmanage.entity.Enum.RoomStatus;
import com.hotelmanage.entity.room.RoomType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "amenity")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Amenity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "amenity_id")
    private Integer amenityId;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "amenity_name")
    private String amenityName;

    @Lob
    @Column(name = "description", columnDefinition = "LONGTEXT")
    String description;
}
