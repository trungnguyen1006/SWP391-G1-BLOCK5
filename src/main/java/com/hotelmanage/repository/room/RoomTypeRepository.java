package com.hotelmanage.repository.room;

import com.hotelmanage.entity.room.RoomType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RoomTypeRepository extends JpaRepository<RoomType, Integer> {

    /**
     * Tìm các loại phòng có phòng trống trong khoảng thời gian và đủ số người
     */
    @Query("""
        SELECT rt, COUNT(DISTINCT r.roomId) as availableCount
        FROM RoomType rt
        JOIN rt.rooms r
        WHERE rt.deletedAt IS NULL
        AND rt.amountPerson >= :amountPerson
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
        GROUP BY rt.roomTypeId
        HAVING COUNT(DISTINCT r.roomId) > 0
        ORDER BY rt.price ASC
    """)
    List<Object[]> findAvailableRoomTypes(@Param("checkInDate") LocalDate checkInDate,
                                          @Param("checkOutDate") LocalDate checkOutDate,
                                          @Param("amountPerson") Integer amountPerson);

    /**
     * Tìm tất cả loại phòng chưa xóa với phân trang
     */
    @Query("SELECT rt FROM RoomType rt WHERE rt.deletedAt IS NULL")
    Page<RoomType> findAllActive(Pageable pageable);

    Page<RoomType> findByRoomTypeNameContainingIgnoreCaseAndDeletedAtIsNull(String keyword, Pageable pageable);
}
