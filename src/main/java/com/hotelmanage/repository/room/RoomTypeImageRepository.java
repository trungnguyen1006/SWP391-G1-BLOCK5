package com.hotelmanage.repository.room;

import com.hotelmanage.entity.room.RoomTypeImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomTypeImageRepository extends JpaRepository<RoomTypeImage, Integer> {
    List<RoomTypeImage> findByRoomType_RoomTypeId(Integer roomTypeId);
}
