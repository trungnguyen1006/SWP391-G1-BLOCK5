package com.hotelmanage.repository.room;

import com.hotelmanage.entity.Enum.RoomStatus;
import com.hotelmanage.entity.room.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface RoomRepository extends JpaRepository<Room, Integer> {

    /**
     * Tìm phòng available cho room type trong khoảng thời gian
     */
    @Query("""
        SELECT r
        FROM Room r
        WHERE r.roomType.roomTypeId = :roomTypeId
        AND r.deletedAt IS NULL
        AND r.status = 'AVAILABLE'
        AND r.roomId NOT IN (
            SELECT b.room.roomId
            FROM Booking b
            WHERE b.status IN ('PENDING', 'CONFIRMED')
            AND (
                (b.checkInDate <= :checkOutDate AND b.checkOutDate >= :checkInDate)
            )
        )
        ORDER BY r.roomNumber ASC
    """)
    List<Room> findAvailableRoomByRoomTypeAndDateRange(
            @Param("roomTypeId") Integer roomTypeId,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate);
    @Query("""
SELECT r FROM Room r
WHERE r.deletedAt IS NULL
AND (
    LOWER(r.roomNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))
    OR LOWER(r.roomType.roomTypeName) LIKE LOWER(CONCAT('%', :keyword, '%'))
    OR LOWER(r.status) LIKE LOWER(CONCAT('%', :keyword, '%'))
)
ORDER BY r.roomNumber ASC
""")
    List<Room> searchRooms(@Param("keyword") String keyword);

    @Query("SELECT r FROM Room r WHERE r.deletedAt IS NULL")
    Page<Room> findAllActive(Pageable pageable);

    @Query("""
    SELECT r FROM Room r
    WHERE r.deletedAt IS NULL
      AND (
        LOWER(r.roomNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR LOWER(r.roomType.roomTypeName) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR LOWER(r.status) LIKE LOWER(CONCAT('%', :keyword, '%'))
      )
""")
    Page<Room> searchActiveRooms(@Param("keyword") String keyword, Pageable pageable);

    // Dashboard queries
    @Query("SELECT COUNT(r) FROM Room r WHERE r.deletedAt IS NULL")
    Long countTotalRooms();

    @Query("SELECT COUNT(r) FROM Room r WHERE r.status = 'AVAILABLE' AND r.deletedAt IS NULL")
    Long countAvailableRooms();


}
