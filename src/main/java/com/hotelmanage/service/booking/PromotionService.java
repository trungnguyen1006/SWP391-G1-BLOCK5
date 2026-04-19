package com.hotelmanage.service.booking;

import com.hotelmanage.entity.booking.Promotion;
import com.hotelmanage.repository.booking.PromotionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PromotionService {

    private final PromotionRepository promotionRepository;

    public List<Promotion> findAll() {
        return promotionRepository.findAll();
    }


    /**
     * Validate và trả về promotion nếu hợp lệ
     */
    public Promotion validatePromotion(String code) {
        log.info("Validating promotion code: {}", code);

        Promotion promotion = promotionRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Mã giảm giá không tồn tại!"));

        LocalDate today = LocalDate.now();

        // Kiểm tra còn hiệu lực
        if (!promotion.getIsActive()) {
            throw new RuntimeException("Mã giảm giá đã hết hiệu lực!");
        }

        // Kiểm tra thời gian
        if (today.isBefore(promotion.getStartDate())) {
            throw new RuntimeException("Mã giảm giá chưa có hiệu lực!");
        }

        if (today.isAfter(promotion.getEndDate())) {
            throw new RuntimeException("Mã giảm giá đã hết hạn!");
        }

        // Kiểm tra số lần dùng
        if (promotion.getUsageLimit() != null &&
                promotion.getUsedCount() >= promotion.getUsageLimit()) {
            throw new RuntimeException("Mã giảm giá đã hết lượt sử dụng!");
        }

        log.info("Promotion code validated successfully: {}", code);
        return promotion;
    }

    /**
     * Tăng số lần đã sử dụng promotion
     */
    public void incrementUsedCount(Integer promotionId) {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy promotion!"));

        promotion.setUsedCount(promotion.getUsedCount() + 1);
        promotionRepository.save(promotion);

        log.info("Incremented used count for promotion: {}, current count: {}",
                promotion.getCode(), promotion.getUsedCount());
    }

    public List<Promotion> findAllActive() {
        LocalDate today = LocalDate.now();
        return promotionRepository.findByIsActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                today, today);
    }

    public Promotion findById(Integer id) {
        return promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy promotion với ID: " + id));
    }

    public Promotion save(Promotion promotion) {
        return promotionRepository.save(promotion);
    }

    public Promotion update(Promotion promotion) {
        Promotion existing = findById(promotion.getPromotionId());
        existing.setCode(promotion.getCode());
        existing.setDiscountAmount(promotion.getDiscountAmount());
        existing.setUsageLimit(promotion.getUsageLimit());
        existing.setStartDate(promotion.getStartDate());
        existing.setEndDate(promotion.getEndDate());
        existing.setIsActive(promotion.getIsActive());
        return promotionRepository.save(existing);
    }

    public void deactivate(Integer id) {
        Promotion promotion = findById(id);
        promotion.setIsActive(false);
        promotionRepository.save(promotion);
    }
}
