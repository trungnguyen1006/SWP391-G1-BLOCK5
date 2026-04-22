package com.hotelmanage.repository.booking;

import com.hotelmanage.entity.booking.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Integer> {

    Optional<Promotion> findByCode(String code);

    Optional<Promotion> findById(Integer id);

    List<Promotion> findByIsActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            LocalDate startDate, LocalDate endDate);

    boolean existsByCode(String code);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Promotion p SET p.usedCount = p.usedCount + 1 WHERE p.promotionId = :id")
    int incrementUsedCount(@Param("id") Integer id);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Promotion p SET p.usedCount = p.usedCount - 1 WHERE p.promotionId = :id AND p.usedCount > 0")
    int decrementUsedCount(@Param("id") Integer id);
}
