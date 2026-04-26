package com.hotelmanage.repository.booking;

import com.hotelmanage.entity.booking.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
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

    boolean existsByCodeAndPromotionIdNot(String code, Integer promotionId);

    // Thêm hàm search và sort
    List<Promotion> findByCodeContainingIgnoreCase(String keyword, org.springframework.data.domain.Sort sort);

    @org.springframework.data.jpa.repository.Query(
            "SELECT COUNT(p) FROM Promotion p WHERE p.isActive = true AND p.startDate <= :today AND p.endDate >= :today")
    long countActivePromotions(@org.springframework.data.repository.query.Param("today") java.time.LocalDate today);
}
