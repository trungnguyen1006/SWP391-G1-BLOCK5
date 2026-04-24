package com.hotelmanage.repository.room;

import com.hotelmanage.entity.room.RoomTypeReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RoomTypeReviewRepository extends JpaRepository<RoomTypeReview, Long> {

    @Query("""
            select r from RoomTypeReview r
            join fetch r.user u
            where r.roomType.roomTypeId = :roomTypeId
            order by r.createdAt desc
            """)
    List<RoomTypeReview> findAllByRoomTypeIdWithUser(@Param("roomTypeId") Integer roomTypeId);

    @Query("""
            select r from RoomTypeReview r
            where r.roomType.roomTypeId = :roomTypeId
            and r.user.id = :userId
            """)
    Optional<RoomTypeReview> findByRoomTypeIdAndUserId(@Param("roomTypeId") Integer roomTypeId,
                                                       @Param("userId") Long userId);

    @Query("""
            select avg(r.rating) from RoomTypeReview r
            where r.roomType.roomTypeId = :roomTypeId
            """)
    Double findAverageRatingByRoomTypeId(@Param("roomTypeId") Integer roomTypeId);

    long countByRoomTypeRoomTypeId(Integer roomTypeId);
}

