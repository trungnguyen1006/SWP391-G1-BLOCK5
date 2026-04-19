package com.hotelmanage.repository.booking;

import com.hotelmanage.entity.booking.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Integer> {

    /**
     * Tìm promotion theo code
     */
    Optional<Promotion> findByCode(String code);

    Optional<Promotion> findById(Integer id);

    /**
     * Tìm các promotion đang active và trong thời gian hiệu lực
     */
    List<Promotion> findByIsActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            LocalDate startDate, LocalDate endDate);

    /**
     * Kiểm tra code đã tồn tại
     */
    boolean existsByCode(String code);
}
